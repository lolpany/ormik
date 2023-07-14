package lol.lolpany.ormik.regression;

import com.google.gson.annotations.SerializedName;

public enum ReadWriteType {
    @SerializedName("read")
    READ_ONLY,
    @SerializedName("write")
    WRITE_ONLY,
    @SerializedName("rw")
    READ_WRITE
}
