package com.github.valet2k.nails;

import com.martiansoftware.nailgun.Alias;
import com.martiansoftware.nailgun.NGContext;

/**
 * Created by automaticgiant on 5/2/16.
 */
public class UUID {
    public static final Alias UUID = new Alias("uuid", "get a UUID, probably for sessionid", UUID.class);
    public static void nailMain(NGContext ctx) {
        ctx.out.print(java.util.UUID.randomUUID());
    }
}
