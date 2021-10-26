package com.wtgroup.sugar.enums;

import com.wtgroup.sugar.consts.StringPool;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MimeType 枚举兼工具
 *
 * @author L&J
 * @date 2021-10-27
 */
@Getter
public enum MimeType {

    // -- text -----------------------------------------------------------------
    CSS("css", "级联样式表（CSS）", "text/css"),
    CSV("csv", "逗号分隔值（CSV）", "text/csv"),
    HTM("htm", "超文本标记语言（HTML）", "text/html"),
    HTML("html", "超文本标记语言（HTML）", "text/html"),
    ICS("ics", "iCalendar格式", "text/calendar"),
    JS("js", "JavaScript", "text/javascript"),
    MJS("mjs", "JavaScript模块", "text/javascript"),
    TXT("txt", "文本(通常为ASCII或ISO 8859-n)", "text/plain"),

    // -- image ----------------------------------------------------------------
    JPEG("jpeg", "JPEG图像", "image/jpeg"),
    JPG("jpg", "JPEG图像", "image/jpeg"),
    PNG("png", "便携式网络图形", "image/png"),
    GIF("gif", "图形交换格式（GIF）", "image/gif"),
    SVG("svg", "可缩放矢量图形（SVG）", "image/svg+xml"),
    TIF("tif", "标记图像文件格式（TIFF）", "image/tiff"),
    TIFF("tiff", "标记图像文件格式（TIFF）", "image/tiff"),
    WEBP("webp", "WEBP图像", "image/webp"),
    BMP("bmp", "Windows OS / 2位图图形", "image/bmp"),
    ICO("ico", "图标格式", "image/vnd.microsoft.icon"),

    // -- audio ----------------------------------------------------------------
    AAC("acc", "AAC音频", "audio/aac"),
    MID("mid", "乐器数字接口（MIDI）", "audio/midi"),
    MIDI("midi", "乐器数字接口（MIDI）", "audio/midi"),
    MP3("mp3", "MP3音频", "audio/mpeg"),
    OGA("oga", "OGG音讯", "audio/ogg"),
    OPUS("opus", "OPUS音频", "audio/opus"),
    WAV("wav", "波形音频格式", "audio/wav"),
    WEBA("weba", "WEBM音频", "audio/webm"),
    MIME_3GP_WITHOUT_VIDEO("3gp", "3GPP audio/video container doesn't contain video", "audio/3gpp2"),
    MIME_3G2_WITHOUT_VIDEO("3g2", "3GPP2 audio/video container  doesn't contain video", "audio/3gpp2"),

    // -- video ----------------------------------------------------------------
    AVI("avi", "音频视频交错格式", "video/x-msvideo"),
    MIME_3GP("3gp", "3GPP audio/video container", "video/3gpp"),
    MIME_3G2("3g2", "3GPP2 audio/video container", "video/3gpp2"),
    MPEG("mpeg", "MPEG视频", "video/mpeg"),
    OGV("ogv", "OGG视频", "video/ogg"),
    TS("ts", "MPEG传输流", "video/mp2t"),
    WEBM("webm", "WEBM视频", "video/webm"),

    // -- font -----------------------------------------------------------------
    OTF("otf", "otf字体", "font/otf"),
    TTF("ttf", "ttf字体", "font/ttf"),
    WOFF("woff", "Web开放字体格式（WOFF）", "font/woff"),
    WOFF2("woff2", "Web开放字体格式（WOFF）", "font/woff2"),

    // -- application ----------------------------------------------------------
    ABW("abw", "AbiWord文件", "application/x-abiword"),
    ARC("arc", "存档文件", "application/x-freearc"),
    AZW("azw", "亚马逊Kindle电子书格式", "application/vnd.amazon.ebook"),
    BIN("bin", "任何类型的二进制数据", "application/octet-stream"),
    BZ("bz", "BZip存档", "application/x-bzip"),
    BZ2("bz2", "BZip2存档", "application/x-bzip2"),
    CSH("csh", "C-Shell脚本", "application/x-csh"),
    DOC("doc", "微软Word文件", "application/msword"),
    DOCX("docx", "Microsoft Word（OpenXML）", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
    EOT("eot", "MS Embedded OpenType字体", "application/vnd.ms-fontobject"),
    EPUB("epub", "电子出版物（EPUB）", "application/epub+zip"),
    GZ("gz", "GZip压缩档案", "application/gzip"),
    JAR("jar", "Java存档", "application/java-archive"),
    JSON("json", "JSON格式", "application/json"),
    JSONLD("jsonld", "JSON-LD格式", "application/ld+json"),
    MPKG("mpkg", "苹果安装程序包", "application/vnd.apple.installer+xml"),
    ODP("odp", "OpenDocument演示文稿文档", "application/vnd.oasis.opendocument.presentation"),
    ODS("ods", "OpenDocument电子表格文档", "application/vnd.oasis.opendocument.spreadsheet"),
    ODT("odt", "OpenDocument文字文件", "application/vnd.oasis.opendocument.text"),
    OGX("ogx", "OGG", "application/ogg"),
    PDF("pdf", "Adobe 可移植文档格式（PDF）", "application/pdf"),
    PHP("php", "php", "application/x-httpd-php"),
    PPT("ppt", "Microsoft PowerPoint", "application/vnd.ms-powerpoint"),
    PPTX("pptx", "Microsoft PowerPoint（OpenXML）", "application/vnd.openxmlformats-officedocument.presentationml.presentation"),
    RAR("rar", "RAR档案", "application/vnd.rar"),
    RTF("rtf", "富文本格式", "application/rtf"),
    SH("sh", "Bourne Shell脚本", "application/x-sh"),
    SWF("swf", "小型Web格式（SWF）或Adobe Flash文档", "application/x-shockwave-flash"),
    TAR("tar", "磁带存档（TAR）", "application/x-tar"),
    VSD("vsd", "微软Visio", "application/vnd.visio"),
    XHTML("xhtml", "XHTML", "application/xhtml+xml"),
    XLS("xls", "微软Excel", "application/vnd.ms-excel"),
    XLSX("xlsx", "微软Excel（OpenXML）", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
    XML("xml", "XML", "application/xml"),
    XUL("xul", "XUL", "application/vnd.mozilla.xul+xml"),
    ZIP("zip", "ZIP", "application/zip"),
    MIME_7Z("7z", "7-zip存档", "application/x-7z-compressed")
    ;

    // 扩展->Enum
    private static final Map<String, WeakReference<MimeType>> EXTENSION_MIME_TYPE_MAP = new ConcurrentHashMap<>();

    //扩展名
    private final String extension;
    //说明
    private final String explain;
    //contentType/mime类型
    private final String mimeType;

    /**
     * @param extension 上传的文件扩展名. '.jpg' | 'jpg'
     * @param explain   类型说明
     * @param mimeType  Mime对应的类型
     */
    MimeType(String extension, String explain, String mimeType) {
        this.extension = extension;
        this.explain = explain;
        this.mimeType = mimeType;
    }

    /**
     * 通过扩展名获取枚举类型
     *
     * @param extension 扩展名
     * @return 枚举类
     */
    public static Optional<MimeType> getByExtension(String extension) {
        if (StringUtils.isEmpty(extension)) {
            return Optional.empty();
        }

        if (extension.startsWith(StringPool.DOT)) {
            extension = extension.substring(1);
        }

        final String finalExtension = extension;
        MimeType res = Optional.ofNullable(EXTENSION_MIME_TYPE_MAP.get(extension))
                .map(WeakReference::get)
                .orElseGet(() -> {
                    for (MimeType typeEnum : MimeType.values()) {
                        if (typeEnum.getExtension().equalsIgnoreCase(finalExtension)) {
                            return typeEnum;
                        }
                    }
                    return null;
                });

        return Optional.ofNullable(res);
    }

    /**
     * Content-Type常用对照 根据后缀获取Mime
     *
     * @param extension 上传的文件扩展名. '.jpg' | 'jpg'
     * @return mime类型
     */
    public static String getContentType(String extension) {
        Optional<MimeType> mimeTypeEnum = MimeType.getByExtension(extension);
        if (mimeTypeEnum.isPresent()) {
            return mimeTypeEnum.get().getMimeType();
        }
        return BIN.getMimeType();
    }

}
