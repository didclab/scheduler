package com.onedatashare.scheduler.model;

import com.onedatashare.scheduler.enums.EndPointType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.io.Serializable;
import java.util.ArrayList;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileSource implements Serializable {
    @NonNull
    private String credId;
    @NonNull
    public EndPointType type;

    @NonNull
    public String fileSourcePath; //can also be the parent Id to the directory to find all data in the infoList

    @NonNull
    public ArrayList<EntityInfo> resourceList; //a list of files and folders. This will end up being a list of only files with paths fully expanded
}
