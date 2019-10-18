package com.caimuhao.rxpicker.bean;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FolderClickEvent {
    private int position;
    private ImageFolder folder;
}
