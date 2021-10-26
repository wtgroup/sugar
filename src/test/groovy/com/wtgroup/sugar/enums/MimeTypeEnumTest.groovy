package com.wtgroup.sugar.enums

import spock.lang.Specification
import spock.lang.Unroll

class MimeTypeEnumTest extends Specification {

    @Unroll
    def "GetByExtension ext=#ext"() {
        expect:
        MimeType.getByExtension(ext).get() == expectResult

        where:
        ext     || expectResult
        ".jpg"  || MimeType.JPG
        "jpg"   || MimeType.JPG
        ".jpeg" || MimeType.JPEG
        ".JPEg" || MimeType.JPEG
        "xls"   || MimeType.XLS
        "avi"   || MimeType.AVI
    }

    @Unroll
    def "GetContentType ext=#ext"() {
        expect:
        MimeType.getContentType(ext) == expectResult

        where:
        ext     || expectResult
        ".jpg"  || "image/jpeg"
        "jpg"   || "image/jpeg"
        ".jpeg" || "image/jpeg"
        ".JPEg" || "image/jpeg"
        "xls"   || "application/vnd.ms-excel"
        "avi"   || "video/x-msvideo"
    }
}
