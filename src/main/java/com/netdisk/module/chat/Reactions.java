package com.netdisk.module.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Reactions {

    List<Long> smile;

    List<Long> heart;

}
