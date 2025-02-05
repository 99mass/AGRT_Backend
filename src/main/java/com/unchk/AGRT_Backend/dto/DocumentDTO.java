package com.unchk.AGRT_Backend.dto;

import com.unchk.AGRT_Backend.enums.DocumentType;
import lombok.Data;



@Data
public
class DocumentDTO {
    private String base64Content;
    private String originalFilename;
    private DocumentType documentType;
}