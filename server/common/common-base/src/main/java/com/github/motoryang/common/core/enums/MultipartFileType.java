package com.github.motoryang.common.core.enums;

import lombok.Getter;

@Getter
public enum MultipartFileType {

    IMAGE("image", false),
    VIDEO("video", false),
    DOCUMENT("document",  false),
    CODE("code",  false),
    ARCHIVE("archive", false),
    AUDIO("audio", false),
    AVATAR("avatar", true),
    FILE("file", false),;

    private final String type;
    private final boolean isPublic;

    MultipartFileType(String type,  boolean isPublic) {
        this.type = type;
        this.isPublic = isPublic;
    }

    public static MultipartFileType getByType(String type) {
        if (type == null) return null;
        for (MultipartFileType mft: MultipartFileType.values()) {
            if (mft.type.equals(type)) {
                return mft;
            }
        }
        return null;
    }

}
