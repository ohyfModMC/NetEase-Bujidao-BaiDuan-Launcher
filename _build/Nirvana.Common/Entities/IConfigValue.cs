using System.Text.Json.Nodes;

namespace Nirvana.Common.Entities;

public interface IConfigValue {
    /**
     * 是否为默认值
     */
    bool IsDefault();

    /**
     * 获取值，值为 null 时返回默认值
     */
    object? GetValueTo();

    /**
     * 名称是否相同_忽略大小写
     */
    bool EqualsName(string name);

    /**
     * 添加到 Json Object
     */
    void ToAdd(JsonObject jsonObj);

    /**
     * 设置值
     */
    void SetFrom(object? value);

    /**
     * 设置默认值
     */
    void SetDefaultFrom(object? value);
}