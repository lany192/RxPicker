package com.caimuhao.rxpicker.bean;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ImageItem implements Serializable, Comparable<ImageItem> {

    /**
     * Image id.
     */
    private int id;
    /**
     * Image path.
     */
    private String path;
    /**
     * Image name.
     */
    private String name;
    /**
     * The time to be added to the library.
     */
    private long addTime;

    public int compareTo(ImageItem o) {
        long time = o.getAddTime() - getAddTime();
        if (time > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        } else if (time < -Integer.MAX_VALUE) return -Integer.MAX_VALUE;
        return (int) time;
    }
}
