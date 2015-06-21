package ch.n1b.schemsub;

import ch.n1b.libschem.LibschemAPI;
import ch.n1b.vector.Vec3D;
import ch.n1b.worldedit.schematic.block.BaseBlock;
import ch.n1b.worldedit.schematic.data.DataException;
import ch.n1b.worldedit.schematic.schematic.Cuboid;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @author Thomas
 * @version schemsub 21.06.2015.
 */
public class Main {

    public static void main(String[] args) {
        if (args.length < 3) {
            System.err.println("Too few arguments.");
            System.out.println("Usage: schemli <old blockid>[:<old blockdata>] <new blockid>[:<new " +
                    "blockdata>] <input schematic" +
                    "(s)> ");
            System.exit(1);
        }

        int oldBlockIdTmp = 0;
        int newBlockIdTmp = 0;
        int oldBlockDataTmp = -1;
        int newBlockDataTmp = 0;
        try {
            if(args[0].contains(":")) {
                String[] block = args[0].split(":");
                oldBlockIdTmp = Integer.parseInt(block[0]);
                oldBlockDataTmp = Integer.parseInt(block[1]);
            }else {
                oldBlockIdTmp = Integer.parseInt(args[0]);
            }
            if(args[1].contains(":")) {
                String[] block = args[1].split(":");
                newBlockIdTmp = Integer.parseInt(block[0]);
                newBlockDataTmp = Integer.parseInt(block[1]);
            }else {
                newBlockIdTmp = Integer.parseInt(args[1]);
            }
        } catch (NumberFormatException e) {
            System.err.println("Cannot parse block ids: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

        final int oldBlockId = oldBlockIdTmp;
        final int newBlockId = newBlockIdTmp;
        final int oldBlockData = oldBlockDataTmp;
        final int newBlockData = newBlockDataTmp;

        List<String> schematics = Arrays.asList(Arrays.copyOfRange(args, 2, args.length));
        schematics.parallelStream().forEach(s -> replaceBlocks(new File(s),oldBlockId,newBlockId,
                oldBlockData,newBlockData));
    }

    private static void replaceBlocks(File infile, int oldBlockId, int newBlockId,int oldBlockData,int
            newBlockData) {
        if(!infile.exists()){
            System.err.println("Cannot find file: " + infile.getName());
            System.exit(1);
        }

        if(!infile.isFile()){
            System.err.println("Not a file: " + infile.getName());
            System.exit(1);
        }

        System.out.println("Loading " + infile.getName());
        Cuboid cuboid = null;
        try {
            cuboid= LibschemAPI.loadSchematic(infile);
        } catch (IOException | DataException e) {
            System.err.println("Cannot load schematic: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

        System.out.println("Replacing " + oldBlockId + ":" + oldBlockData+ " with " + newBlockId + ":" + newBlockData);
        cuboid = replace(cuboid, oldBlockId, newBlockId,oldBlockData,newBlockData);

        try {
            System.out.println("Saving " + infile.getName());
            LibschemAPI.saveSchematic(infile,cuboid);
        } catch (IOException | DataException e) {
            System.err.println("Cannot save schematic: " + infile.getName());
            e.printStackTrace();
            System.exit(1);
        }

        System.out.println("Done.");
    }

    private static Cuboid replace(Cuboid cuboid, int oldBlockId, int newBlockId,int oldBlockData,int
            newBlockData) {
        for(int x=0;x<cuboid.getWidth();x++){
            for (int y = 0; y < cuboid.getHeight(); y++) {
                for (int z = 0; z < cuboid.getLength(); z++) {
                    Vec3D vec = new Vec3D(x, y, z);
                    BaseBlock block = cuboid.getBlock(vec);
                    if (block.getType() == oldBlockId && (oldBlockData < 0 || oldBlockData==block.getData())) {
                        BaseBlock newBlock = new BaseBlock(newBlockId,newBlockData);
                        cuboid.setBlock(vec,newBlock);
                    }
                }
            }
        }
        return cuboid;
    }
}
