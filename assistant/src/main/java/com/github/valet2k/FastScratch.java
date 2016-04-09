package com.github.valet2k;

import com.martiansoftware.nailgun.Alias;
import com.martiansoftware.nailgun.AliasManager;
import com.martiansoftware.nailgun.NGContext;
import com.martiansoftware.nailgun.NGServer;
import org.apache.commons.io.IOUtils;

import java.io.IOException;

/**
 * Created by automaticgiant on 4/8/16.
 */
public class FastScratch {
    public static void nailMain(NGContext ctx) throws IOException {
//        BufferedInputStream in = new BufferedInputStream(ctx.in);
        System.out.println(IOUtils.toString(ctx.in));
    }
    public static void main(String args[]) {
        NGServer ngServer = new NGServer();
        AliasManager aliasManager = ngServer.getAliasManager();
        aliasManager.addAlias(new Alias("sc","",FastScratch.class));
        System.err.println("starting");
        ngServer.run();
        System.err.println("done");
    }
}
