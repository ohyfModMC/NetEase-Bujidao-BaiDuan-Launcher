using Microsoft.AspNetCore.Mvc;
using Nirvana.Common.Utils.CodeTools;
using Nirvana.Public.Entities.NEL;
using Nirvana.Public.Message;
using Nirvana.WPFLauncher.Protocol;

namespace Fantnel.Servlet.GameController;

[ApiController]
[Route("[controller]")]
public class GameSkinController : ControllerBase {
    [HttpGet("/api/gameskin/get")]
    public IActionResult GetServerHttp([FromQuery] int offset = 0, [FromQuery] int pageSize = 10, [FromQuery] string? name = null)
    {
        var entity = name == null ? SkinMessage.GetSkinList(offset, pageSize).GetAwaiter().GetResult() : SkinMessage.GetSkinListByName(name, offset, pageSize).GetAwaiter().GetResult();
        return Ok(Code.ToJson(ErrorCode.Success, entity));
    }

    [HttpGet("/api/gameskin/detail")]
    public IActionResult GetSkinDetailHttp([FromQuery] string id)
    {
        var skinDetail = new EntitySkinDetail(id);
        return Ok(Code.ToJson(ErrorCode.Success, skinDetail));
    }

    [HttpGet("/api/gameskin/set")]
    public IActionResult SetSkinHttp([FromQuery] string id)
    {
        NPFLauncher.SetSkinAsync(id).GetAwaiter().GetResult();
        return Ok(Code.ToJson(ErrorCode.Success));
    }
}