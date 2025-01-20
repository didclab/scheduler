package com.onedatashare.scheduler.model;

import com.onedatashare.scheduler.enums.EndPointType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileDestination implements Serializable {

    @NonNull
    private String credId;

    @NonNull
    private EndPointType type;

    @NonNull
    String fileDestinationPath;

}
