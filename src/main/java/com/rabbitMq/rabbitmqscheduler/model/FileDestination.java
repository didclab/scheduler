package com.rabbitMq.rabbitmqscheduler.model;

import com.rabbitMq.rabbitmqscheduler.enums.EndPointType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileDestination {

    @NonNull
    private String credId;

    @NonNull
    private EndPointType type;

    @NonNull
    String destinationPath;

}
