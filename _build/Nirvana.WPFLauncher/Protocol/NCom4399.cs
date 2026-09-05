using System;
using System.Net;
using System.Net.Http;
using System.Text.Json;
using System.Threading.Tasks;
using Nirvana.WPFLauncher.Entities.Pc4399.Com4399;
using Nirvana.WPFLauncher.Utils;

namespace Nirvana.WPFLauncher.Protocol;

public static class NCom4399 {
    private static readonly HttpClient Client = new(new HttpClientHandler {
        UseCookies = true,
        CookieContainer = new CookieContainer(),
        AutomaticDecompression = DecompressionMethods.All
    }) {
        Timeout = TimeSpan.FromSeconds(30)
    };

    static NCom4399() {
        // 浏览器伪装请求头: 4399 风控会拒绝裸 HttpClient (返回 202 + "请稍后再试~")
        Client.DefaultRequestHeaders.UserAgent.ParseAdd("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36");
        Client.DefaultRequestHeaders.Accept.ParseAdd("text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8");
        Client.DefaultRequestHeaders.AcceptLanguage.ParseAdd("zh-CN,zh;q=0.9,en;q=0.8");
        Client.DefaultRequestHeaders.Add("sec-ch-ua", "\"Chromium\";v=\"121\", \"Not?A_Brand\";v=\"24\"");
        Client.DefaultRequestHeaders.Add("sec-ch-ua-platform", "\"Windows\"");
        Client.DefaultRequestHeaders.Add("sec-fetch-dest", "document");
        Client.DefaultRequestHeaders.Add("sec-fetch-mode", "navigate");
        Client.DefaultRequestHeaders.Add("sec-fetch-site", "none");
        Client.DefaultRequestHeaders.Add("sec-fetch-user", "?1");
        Client.DefaultRequestHeaders.Add("Upgrade-Insecure-Requests", "1");
    }

    public static string LoginWithPassword(string username, string password, string sessionId, string captcha)
    {
        return LoginWithPasswordAsync(username, password, sessionId, captcha).GetAwaiter().GetResult();
    }

    private static async Task<string> LoginWithPasswordAsync(string username, string password, string sessionId, string captcha)
    {
        var oauthResp = await Client.GetAsync("https://m.4399api.com/openapi/oauth-callback.html?gamekey=44770&game_key=115716");
        var oauthText = await oauthResp.Content.ReadAsStringAsync();
        Console.Error.WriteLine("[DBG][NCom4399] oauth-callback 状态=" + (int)oauthResp.StatusCode + " 长度=" + oauthText.Length + " 前120字=" + (oauthText.Length > 120 ? oauthText[..120] : oauthText));
        Entity4399OAuthCallback oauthCallback;
        try {
            oauthCallback = JsonSerializer.Deserialize<Entity4399OAuthCallback>(oauthText) ?? throw new Exception("Failed to deserialize: " + oauthText);
        } catch (JsonException) {
            Console.Error.WriteLine("[DBG][NCom4399] oauth-callback 原始响应(前300字): " + (oauthText.Length > 300 ? oauthText[..300] : oauthText));
            throw;
        }

        var queryParams = QueryBuilder.FromParameters(oauthCallback.Result);
        var clientId = queryParams.Get("client_id");
        var state = queryParams.Get("state");
        var ref1 = queryParams.Get("ref");

        // 构建登录参数
        var parameters = BuildLoginParameters(clientId, state, ref1);
        parameters.Add("username", username);
        parameters.Add("password", password);
        parameters.Add("captcha_id", captcha);
        parameters.Add("captcha", sessionId);

        // 执行登录请求 (带 Referer/Origin, 防风控)
        var loginRequest = new HttpRequestMessage(HttpMethod.Post, "https://ptlogin.4399.com/oauth2/loginAndAuthorize.do?channel=&sdk=op&sdk_version=3.14.5.577") {
            Content = new FormUrlEncodedContent(parameters.GetAll())
        };
        loginRequest.Headers.Referrer = new Uri("https://ptlogin.4399.com/");
        loginRequest.Headers.Add("Origin", "https://ptlogin.4399.com");
        var loginResponse = await Client.SendAsync(loginRequest);

        var loginText = await loginResponse.Content.ReadAsStringAsync();
        Console.Error.WriteLine("[DBG][NCom4399] login 状态=" + (int)loginResponse.StatusCode + " 长度=" + loginText.Length + " 前120字=" + (loginText.Length > 120 ? loginText[..120] : loginText));

        // 找到错误信息
        var errText = ExtractErrorTip(loginText);
        if (errText.Length > 0) {
            throw new Exception(errText);
        }

        // 4399 风控响应: HTTP 202 + 纯文本 "请稍后再试~" (非 JSON)
        if (loginResponse.StatusCode == HttpStatusCode.Accepted || loginText.Length == 0 || loginText[0] != '{') {
            throw new Exception("4399 登录被限流/风控拦截 (HTTP " + (int)loginResponse.StatusCode + ", 返回: " + loginText + ")");
        }

        Entity4399UserInfoResponse userInfoResponse;
        try {
            userInfoResponse = JsonSerializer.Deserialize<Entity4399UserInfoResponse>(loginText) ?? throw new Exception("Failed to deserialize: " + loginText);
        } catch (JsonException) {
            Console.Error.WriteLine("[DBG][NCom4399] login 原始响应(前500字): " + (loginText.Length > 500 ? loginText[..500] : loginText));
            throw;
        }

        if (userInfoResponse.Code != "100") {
            throw new Exception(userInfoResponse.Message);
        }

        var entity4399UserInfoResult = userInfoResponse.Result;

        // ReSharper disable once ConvertIfStatementToReturnStatement
        if (entity4399UserInfoResult == null) {
            throw new Exception("Failed to deserialize: " + loginText);
        }

        // 生成SAuth令牌
        return MgbSdk.GenerateSAuth(entity4399UserInfoResult.Uid.ToString(), entity4399UserInfoResult.State, "4399com", "ad");
    }

    private static string ExtractErrorTip(string html)
    {
        const string startMarker = "login_err_msg\">";
        const string endMarker = "</p>";

        var startIndex = html.IndexOf(startMarker, StringComparison.Ordinal);
        if (startIndex == -1) {
            return string.Empty;
        }

        startIndex += startMarker.Length;
        var endIndex = html.IndexOf(endMarker, startIndex, StringComparison.Ordinal);

        if (endIndex == -1) {
            return string.Empty;
        }

        // 提取内容并删除前后空格
        var content = html.Substring(startIndex, endIndex - startIndex);
        return content.Trim();
    }

    private static QueryBuilder BuildLoginParameters(string clientId, string state, string ref1)
    {
        var queryBuilder = new QueryBuilder();
        queryBuilder.Add("isInputRealname", "false");
        queryBuilder.Add("isVaildRealname", "false");
        queryBuilder.Add("sec", "0");
        queryBuilder.Add("client_id", clientId);
        queryBuilder.Add("state", state);
        queryBuilder.Add("ref", ref1);
        queryBuilder.Add("response_type", "TOKEN");
        queryBuilder.Add("scope", "basic");
        queryBuilder.Add("bizId", "2100001792");
        queryBuilder.Add("auth_action", "ORILOGIN");
        queryBuilder.Add("redirect_uri", "https://m.4399api.com/openapi/oauth-callback.html?gamekey=44770&game_key=115716");
        return queryBuilder;
    }
}
