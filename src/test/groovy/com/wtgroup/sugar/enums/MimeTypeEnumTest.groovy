package com.wtgroup.sugar.enums

import spock.lang.Specification
import spock.lang.Unroll

class MimeTypeEnumTest extends Specification {

    @Unroll
    def "GetByExtension ext=#ext"() {
        expect:
        MimeTypeEnum.getByExtension(ext).get() == expectResult

        where:
        ext     || expectResult
        ".jpg"  || MimeTypeEnum.JPG
        "jpg"   || MimeTypeEnum.JPG
        ".jpeg" || MimeTypeEnum.JPEG
        ".JPEg" || MimeTypeEnum.JPEG
        "xls"   || MimeTypeEnum.XLS
        "avi"   || MimeTypeEnum.AVI
    }

    @Unroll
    def "GetContentType ext=#ext"() {
        expect:
        MimeTypeEnum.getContentType(ext) == expectResult

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
