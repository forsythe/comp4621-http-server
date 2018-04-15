package com.heng.message;

//https://developer.mozilla.org/en-US/docs/Web/HTTP/Basics_of_HTTP/MIME_types/Complete_list_of_MIME_types
class ContentType {
    static String getContentType(String fileExtension) {
        switch (fileExtension.toLowerCase()) {
            //TEXT
            case "txt":
            case "java":
                return "text/plain";
            case "html":
            case "htm":
                return "text/html";
            case "css":
                return "text/css";
            case "csv":
                return "text/csv";

            //IMAGE
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            case "svg":
                return "image/svg+xml svg";
            case "tiff":
                return "image/tiff";
            case "webp":
                return "image/webp";

            //AUDIO
            case "midi":
            case "mid":
                return "audio/midi";
            case "mp3":
                return "audio/mpeg";
            case "ogg":
                return "audio/ogg";
            case "wav":
                return "audio/wav";
            case "aac":
                return "audio/aac";
            case "weba":
                return "audio/webm";

            //VIDEO
            case "avi":
                return "video/x-msvideo";
            case "mpeg":
                return "video/mpeg";
            case "ogv":
                return "video/ogg";
            case "flv":
                return "vdieo/x-flv";
            case "mp4":
                return "video/mp4";
            case "mov":
                return "video/quicktime";
            case "wmv":
                return "video/x-ms-wmv";

            //APPLICATION
            case "pdf":
                return "application/pdf";
            case "pptx":
                return "application/vnd.openxmlformats-officedocument.presentationml.presentation";
            case "ppt":
                return "application/vnd.ms-powerpoint";
            default:
                return "application/octet-stream"; //aka unknown
        }
    }
}
