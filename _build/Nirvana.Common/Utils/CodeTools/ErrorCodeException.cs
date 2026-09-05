using System;
using Nirvana.Common.Entities;

namespace Nirvana.Common.Utils.CodeTools;

public class ErrorCodeException(ErrorCode errorCode, object? data = null) : Exception(Code.GetMessage(errorCode)) {
    public readonly EntityResponse<object> Entity = Code.ToJson(errorCode, data);

    public ErrorCodeException() : this(ErrorCode.Failure) { }

    public ErrorCodeException(object? data = null) : this(ErrorCode.Failure, data) { }

    // private new object? Data { get; } = data;
    // private ErrorCode ErrorCode { get; } = errorCode;

    public EntityResponse<object> GetJson()
    {
        return Entity;
    }
}