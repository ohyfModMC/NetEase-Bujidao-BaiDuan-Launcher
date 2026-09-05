using System;
using Microsoft.AspNetCore.Mvc;
using Nirvana.Common;
using Nirvana.Common.Utils.CodeTools;
using Nirvana.Public.Manager;

namespace Fantnel.Servlet.OthersController;

[ApiController]
[Route("[controller]")]
public class NirvanaController : ControllerBase {
    // 登录账号
    [HttpGet("/api/nirvana/login")]
    public IActionResult Login(string account, string password)
    {
        NirvanaAccountManager.Login(account, password).GetAwaiter().GetResult();
        return Ok(Code.ToJson(ErrorCode.Success));
    }

    // 退出登录
    [HttpGet("/api/nirvana/logout")]
    public IActionResult Logout()
    {
        NirvanaConfig.Logout();
        return Ok(Code.ToJson(ErrorCode.Success));
    }

    // 获取账号
    [HttpGet("/api/nirvana/account/get")]
    public IActionResult GetAccount()
    {
        var entity = NirvanaAccountManager.GetLoginInfo();
        return Ok(Code.ToJson(ErrorCode.Success, entity));
    }

    // 添加配置选项
    [HttpGet("/api/nirvana/add")]
    public IActionResult AddConfig(string name, string? value, string? property)
    {
        NirvanaConfig.AddByTypeName(name, value, property);
        return Ok(Code.ToJson(ErrorCode.Success));
    }

    // 设置配置
    [HttpGet("/api/nirvana/set")]
    public IActionResult SetConfig(string mode, string? value)
    {
        if ("gameMemory".Equals(mode, StringComparison.OrdinalIgnoreCase)) {
            NirvanaConfig.SetGameMemory(value);
        } else if ("chatEnable".Equals(mode, StringComparison.OrdinalIgnoreCase)) {
            NirvanaAccountManager.SetChatEnable(value);
        } else {
            NirvanaConfig.SetValue(mode, value);
        }

        return Ok(Code.ToJson(ErrorCode.Success));
    }

    // 获取配置
    [HttpGet("/api/nirvana/get")]
    public IActionResult GetConfig()
    {
        var config = NirvanaConfig.GetJsonObject();
        return Ok(Code.ToJson(ErrorCode.Success, config));
    }
}