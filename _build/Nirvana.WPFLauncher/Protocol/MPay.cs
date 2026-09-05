using System;
using System.Text.Encodings.Web;
using System.Text.Json;
using System.Threading.Tasks;
using Nirvana.Common;
using Nirvana.Common.Utils.CodeTools;
using Nirvana.WPFLauncher.Entities.MPay;
using Nirvana.WPFLauncher.Http;
using Nirvana.WPFLauncher.Utils;
using Serilog;

namespace Nirvana.WPFLauncher.Protocol;

public class MPay : IDisposable {
    private static readonly JsonSerializerOptions DefaultOptions = new() {
        Encoder = JavaScriptEncoder.UnsafeRelaxedJsonEscaping
    };

    private readonly HttpWrapper _client = new();
    private readonly EntityDevice _device;

    private readonly HttpWrapper _service = new("https://service.mkey.163.com");

    private readonly string _unique;

    public MPay(string gameId = "aecfrxodyqaaaajp-g-x19")
    {
        GameId = gameId;
        _unique = CreateOrLoadUnique();
        _device = CreateOrLoadDevice(gameId);
    }

    private string GameId { get; }

    public void Dispose()
    {
        _client.Dispose();
        _service.Dispose();
        GC.SuppressFinalize(this);
    }

    public EntityDevice GetDevice()
    {
        return _device;
    }

    private static string CreateOrLoadUnique()
    {
        return NirvanaConfig.GetValue("netease_unique", CreateUnique);
    }

    private static string CreateUnique()
    {
        return Guid.NewGuid().ToString().Replace("-", "");
    }

    private EntityDevice CreateOrLoadDevice(string gameId)
    {
        return NirvanaConfig.GetValue("netease_device", () => CreateDevice(gameId));
    }

    private EntityDevice CreateDevice(string gameId)
    {
        return CreateDeviceAsync(gameId).GetAwaiter().GetResult();
    }

    private async Task<EntityDevice> CreateDeviceAsync(string gameId)
    {
        var buildDeviceParams = BuildDeviceParams();
        buildDeviceParams.Add("unique_id", _unique);
        var obj = await _service.PostAsync("/mpay/games/" + gameId + "/devices", buildDeviceParams.BuildQuery(), "application/x-www-form-urlencoded");
        obj.EnsureSuccessStatusCode();
        var text = await obj.Content.ReadAsStringAsync();
        return JsonSerializer.Deserialize<EntityDeviceResponse>(text)?.EntityDevice ?? throw new Exception("Failed to create device");
    }

    public EntityMPayUserResponse LoginWithEmail(string email, string password)
    {
        return LoginWithEmailAsync(email, password).GetAwaiter().GetResult();
    }

    private async Task<EntityMPayUserResponse> LoginWithEmailAsync(string email, string password)
    {
        var value = JsonSerializer.Serialize(new EntityUsersParameters {
            Password = password.EncodeMd5(),
            Unique = _unique,
            Username = email
        }, DefaultOptions).EncodeAes(_device.Key.DecodeHex()).EncodeHex();
        var queryString = BuildBaseParams();
        queryString.Add("opt_fields", "nickname,avatar,realname_status,mobile_bind_status,mask_related_mobile,related_login_status");
        queryString.Add("params", value);
        queryString.Add("un", email.EncodeBase64());
        var response = await _service.PostAsync($"/mpay/games/{GameId}/devices/{_device.Id}/users", queryString.BuildQuery(), "application/x-www-form-urlencoded");
        var text = await response.Content.ReadAsStringAsync();
        if (!response.IsSuccessStatusCode) {
            var entityVerifyResponse = JsonSerializer.Deserialize<EntityVerifyResponse>(text);
            if (entityVerifyResponse is null) {
                throw new Exception("Failed to login with email, response: " + text);
            }

            throw new ErrorCodeException(entityVerifyResponse) {
                Entity = {
                    Code = entityVerifyResponse.Code,
                    Data = entityVerifyResponse,
                    Message = entityVerifyResponse.Reason
                }
            };
        }

        return JsonSerializer.Deserialize<EntityMPayUserResponse>(text) ?? throw new Exception("Failed to login with email, response: " + text);
    }

    public async Task<bool> SendSmsCodeAsync(string phoneNumber)
    {
        var queryBuilder = BuildBaseParams();
        queryBuilder.Add("device_id", _device.Id);
        queryBuilder.Add("mobile", phoneNumber);
        var response = await _service.PostAsync("/mpay/api/users/login/mobile/get_sms", queryBuilder.BuildQuery(), "application/x-www-form-urlencoded");
        var propertyValue = await response.Content.ReadAsStringAsync();
        if (!response.IsSuccessStatusCode) {
            Log.Error("Failed to send sms code, response: {Json}", propertyValue);
        }

        return response.IsSuccessStatusCode;
    }

    public async Task<EntitySmsTicket?> VerifySmsCodeAsync(string phoneNumber, string code)
    {
        var queryBuilder = BuildBaseParams();
        queryBuilder.Add("device_id", _device.Id);
        queryBuilder.Add("mobile", phoneNumber);
        queryBuilder.Add("smscode", code);
        queryBuilder.Add("up_content", "");
        var response = await _service.PostAsync("/mpay/api/users/login/mobile/verify_sms", queryBuilder.BuildQuery(), "application/x-www-form-urlencoded");
        var text = await response.Content.ReadAsStringAsync();
        if (response.IsSuccessStatusCode) {
            return JsonSerializer.Deserialize<EntitySmsTicket>(text);
        }

        Log.Error("Failed to send sms code, response: {Json}", text);
        return null;
    }

    public async Task<EntityMPayUserResponse?> FinishSmsCodeAsync(string phoneNumber, string ticket)
    {
        var text = phoneNumber.EncodeBase64();
        var queryBuilder = BuildBaseParams();
        queryBuilder.Add("device_id", _device.Id);
        queryBuilder.Add("opt_fields", "nickname,avatar,realname_status,mobile_bind_status,mask_related_mobile,related_login_status");
        queryBuilder.Add("ticket", ticket);
        var response = await _service.PostAsync("/mpay/api/users/login/mobile/finish?un=" + text, queryBuilder.BuildQuery(), "application/x-www-form-urlencoded");
        var text2 = await response.Content.ReadAsStringAsync();
        if (response.IsSuccessStatusCode) {
            return JsonSerializer.Deserialize<EntityMPayUserResponse>(text2);
        }

        Log.Error("Failed to finish sms code, response: {Json}", text2);
        return null;
    }


    private QueryBuilder BuildBaseParams()
    {
        var queryBuilder = new QueryBuilder();
        queryBuilder.Add("app_channel", "netease");
        queryBuilder.Add("app_mode", "2").Add("app_type", "games");
        queryBuilder.Add("arch", "win_x64");
        queryBuilder.Add("cv", "c4.2.0");
        queryBuilder.Add("mcount_app_key", "EEkEEXLymcNjM42yLY3Bn6AO15aGy4yq");
        queryBuilder.Add("mcount_transaction_id", "0");
        queryBuilder.Add("process_id", $"{Environment.ProcessId}");
        queryBuilder.Add("sv", "10.0.22621");
        queryBuilder.Add("updater_cv", "c1.0.0");
        queryBuilder.Add("game_id", GameId);
        queryBuilder.Add("gv", X19.GameVersion);
        return queryBuilder;
    }

    private QueryBuilder BuildDeviceParams()
    {
        var queryBuilder = BuildBaseParams();
        queryBuilder.Add("brand", "Microsoft");
        queryBuilder.Add("device_model", "pc_mode");
        queryBuilder.Add("device_name", "PC-" + StringGenerator.GenerateRandomString(12));
        queryBuilder.Add("device_type", "Computer");
        queryBuilder.Add("init_urs_device", "0");
        queryBuilder.Add("mac", StringGenerator.GenerateRandomMacAddress());
        queryBuilder.Add("resolution", "1920x1080");
        queryBuilder.Add("system_name", "windows");
        queryBuilder.Add("system_version", "10.0.22621");
        return queryBuilder;
    }
}