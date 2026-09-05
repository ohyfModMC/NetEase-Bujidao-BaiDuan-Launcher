using System;
using System.Collections.Generic;
using System.IO;
using Nirvana.Common.Utils;
using Serilog;

namespace Nirvana.Cipher.Yggdrasil;

public static class Md5Mapping {
    private static readonly Dictionary<string, Md5Pair> Mapping = new() {
        { "1.7.10", new Md5Pair("A895FE657915D58F55919CEACD30209D", "538D33D5F35EF01736EDA30F94C61DF6") },
        { "1.8.9", new Md5Pair("A895FE657915D58F55919CEACD30209D", "0CF2074AA7D4B543E35A3D6BB57AF861") },
        { "1.16", new Md5Pair("7B101583C3965371B89A3C9115B27526", "B0712F34B0A584D05D9D29FA68759E29") },
        { "1.12.2", new Md5Pair("A895FE657915D58F55919CEACD30209D", "51581ADD89B8AC5A0D8CCDD0E33EE1DE") },
        { "1.18", new Md5Pair("C3BD2115F23F6FE4B2ADCC7FC4DEFFEA", "56677A2BB31E18246FA241FB02E16D0E") },
        { "1.20", new Md5Pair("2A7A476411A1687A56DC6848829C1AE4", "D285CBF97D9BA30D3C445DBF1C342634") },
        { "1.21", new Md5Pair("684528BF492A84489F825F5599B3E1C6", "574033E7E4841D8AC4C14D7FA5E05337") },
        { "1.21.8", new Md5Pair("5BF6153C69DD28951A699F7F834EFE1A", "C9906B5809A92C73299279E562A78D81") }
    };

    public static Md5Pair GetMd5FromGameVersion(string version)
    {
        var pair = Mapping.GetValueOrDefault(version);
        if (pair == null) {
            Log.Error("[Yggdrasil]: 该版本不被 Nirvana 支持: {0}", version);
            throw new ArgumentException($"不受支持的游戏版本: {version}");
        }

        var versionPath = Path.Combine(PathUtil.GameBaseMcPath, "versions", version);
        var datFilePath = Path.Combine(versionPath, version + ".dat");
        if (File.Exists(datFilePath)) {
            var datFileMd5 = FileUtil.ComputeMd5FromFile(datFilePath);
            if (datFileMd5.Length > 31) {
                Log.Information("[Yggdrasil]: (Dat){0}", datFileMd5);
                pair.DatFileMd5 = datFileMd5;
            }
        }

        return pair;
    }

    public class Md5Pair(string bootstrapMd5, string datFileMd5) {
        public readonly string BootstrapMd5 = bootstrapMd5;
        public string DatFileMd5 = datFileMd5;
    }
}