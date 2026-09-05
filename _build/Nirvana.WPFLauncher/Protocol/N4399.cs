using System;
using System.Net;
using System.Net.Http;
using System.Text;
using System.Text.Json;
using System.Threading.Tasks;
using Nirvana.WPFLauncher.Entities.Pc4399;
using QueryBuilder = Nirvana.WPFLauncher.Utils.QueryBuilder;

namespace Nirvana.WPFLauncher.Protocol;

public static class N4399 {
    public static async Task<string> LoginWithPasswordAsync(string username, string password, string sessionId, string captcha)
    {
        // 构建登录参数
        var parameters = BuildLoginParameters();
        parameters.Add("username", username);
        parameters.Add("password", password);
        parameters.Add("sessionId", sessionId);
        parameters.Add("inputCaptcha", captcha);

        var client = new HttpClient(new HttpClientHandler {
            AllowAutoRedirect = true,
            UseCookies = true,
            CookieContainer = new CookieContainer()
        });

        var request = new HttpRequestMessage(HttpMethod.Post, "https://ptlogin.4399.com/ptlogin/login.do?v=1");
        request.Headers.Add("Referer", "http://ptlogin.4399.com/ptlogin/loginFrame.do");
        request.Content = new StringContent(parameters.BuildQuery(), Encoding.UTF8, "application/x-www-form-urlencoded");
        ;

        var loginResponse = await client.SendAsync(request);

        var loginText = await loginResponse.Content.ReadAsStringAsync();

        // 找到错误信息
        var errText = ExtractErrorTip(loginText);
        if (errText.Length > 0) {
            throw new Exception(errText);
        }

        // client.DefaultRequestHeaders.Add("Cookie", loginResponse.Headers.GetValues("Set-Cookie"));

        // 生成SAuth令牌
        var sAuthToken = await GenerateSAuthAsync(client);
        return sAuthToken;
    }

    private static string ExtractErrorTip(string html)
    {
        if (!html.Contains("<html>")) {
            return html.Trim();
        }

        const string startMarker = "login_err_tip\">";
        const string endMarker = "</div>";

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

    private static async Task<string> GenerateSAuthAsync(HttpClient client)
    {
        var unixTimeSeconds = DateTimeOffset.UtcNow.ToUnixTimeSeconds();
        var loginQuery = new QueryBuilder();
        loginQuery.Add("v", "2018_11_26_16");
        loginQuery.Add("postLoginHandler", "redirect");
        loginQuery.Add("checkLoginUserCookie", "true");
        loginQuery.Add("redirectUrl", $"https://cdn.h5wan.4399sj.com/microterminal-h5-frame?game_id=500352&rand_time={unixTimeSeconds}");

        var ucenterQuery = new QueryBuilder();
        ucenterQuery.Add("action", "login");
        ucenterQuery.Add("appId", "kid_wdsj");
        ucenterQuery.Add("loginLevel", "8");
        ucenterQuery.Add("regLevel", "8");
        ucenterQuery.Add("bizId", "2201001794");
        ucenterQuery.Add("externalLogin", "qq");
        ucenterQuery.Add("qrLogin", "true");
        ucenterQuery.Add("layout", "vertical");
        ucenterQuery.Add("level", "101");
        ucenterQuery.Add("css", $"https://microgame.5054399.net/v2/resource/cssSdk/default/login.css?{loginQuery}");

        var queryBuilder = new QueryBuilder();
        queryBuilder.Add("appId", "kid_wdsj");
        queryBuilder.Add("gameUrl", "https://cdn.h5wan.4399sj.com/microterminal-h5-frame?game_id=500352");
        queryBuilder.Add("isCrossDomain", "1");
        queryBuilder.Add("nick", "null");
        queryBuilder.Add("onLineStart", "false");
        queryBuilder.Add("ptLogin", "true");
        queryBuilder.Add("rand_time", "$randTime");
        queryBuilder.Add("retUrl", $"https://ptlogin.4399.com/resource/ucenter.html?{ucenterQuery}");
        queryBuilder.Add("show", "1");

        // 检查登录状态
        var checkUrl = $"https://ptlogin.4399.com/ptlogin/checkKidLoginUserCookie.do?{queryBuilder}";

        var checkResponse = await client.GetAsync(checkUrl);
        if (checkResponse.RequestMessage == null || checkResponse.RequestMessage.RequestUri == null) {
            throw new Exception("登录状态检查失败");
        }

        var redirectUri = checkResponse.RequestMessage.RequestUri.ToString();
        var queryParams = QueryBuilder.FromParameters(redirectUri);
        if (!queryParams.Contains("sig")) {
            throw new Exception("登录状态检查失败");
        }

        // 获取统一认证信息
        var uniAuth = await GetUniAuthAsync(queryParams, client);

        // 生成SAuth令牌
        return MgbSdk.GenerateSAuth(uniAuth.Get("uid"), uniAuth.Get("token"), "4399pc", "pc", uniAuth.Get("username"), uniAuth.Get("time"));
    }

    private static async Task<QueryBuilder> GetUniAuthAsync(QueryBuilder queryParams, HttpClient client)
    {
        var queryStr = new QueryBuilder();
        queryStr.Add("game_id", "500352");
        queryStr.Add("nick", "null");
        queryStr.Add("sig", queryParams.Get("sig"));
        queryStr.Add("uid", queryParams.Get("uid"));
        queryStr.Add("fcm", "0");
        queryStr.Add("isCrossDomain", "1");
        queryStr.Add("show", "1");
        queryStr.Add("rand_time", "$randTime");
        queryStr.Add("ptusertype", "4399");
        queryStr.Add("ptLogin", "true");
        queryStr.Add("time", queryParams.Get("time"));
        queryStr.Add("validateState", queryParams.Get("validateState"));
        queryStr.Add("username", queryParams.Get("username"));

        var queryBuilder = new QueryBuilder();
        queryBuilder.Add("_", queryParams.Get("time"));
        queryBuilder.Add("queryStr", queryStr);

        var response = await client.GetAsync($"https://microgame.5054399.net/v2/service/sdk/info?{queryBuilder}");

        var responseText = await response.Content.ReadAsStringAsync();
        var uniAuthData = JsonSerializer.Deserialize<EntityC4399UniAuth>(responseText) ?? throw new Exception("解析统一认证数据失败");

        return new QueryBuilder(uniAuthData.Data.SdkLoginData);
    }

    private static QueryBuilder BuildLoginParameters()
    {
        var queryBuilder = new QueryBuilder();
        queryBuilder.Add("postLoginHandler", "default");
        queryBuilder.Add("layoutSelfAdapting", "true");
        queryBuilder.Add("externalLogin", "qq");
        queryBuilder.Add("displayMode", "popup");
        queryBuilder.Add("layout", "vertical");
        queryBuilder.Add("bizId", "2201001794");
        queryBuilder.Add("appId", "kid_wdsj");
        queryBuilder.Add("gameId", "wd");
        // queryBuilder.Add("css", "https://microgame.5054399.net/v2/resource/cssSdk/default/login.css");
        queryBuilder.Add("redirectUrl", "");
        queryBuilder.Add("sessionId", "");
        queryBuilder.Add("mainDivId", "popup_login_div");
        queryBuilder.Add("includeFcmInfo", "false");
        queryBuilder.Add("level", "8");
        queryBuilder.Add("regLevel", "8");
        queryBuilder.Add("userNameLabel", "4399用户名");
        queryBuilder.Add("userNameTip", "请输入4399用户名");
        queryBuilder.Add("welcomeTip", "欢迎回到4399");
        queryBuilder.Add("sec", "1");
        // password
        queryBuilder.Add("iframeId", "popup_login_frame");
        // username
        queryBuilder.Add("autoLogin", "on");
        return queryBuilder;
    }
}