package pikachu;

import kaptainwutax.biomeutils.source.OverworldBiomeSource;
import kaptainwutax.featureutils.structure.Stronghold;
import kaptainwutax.featureutils.structure.generator.piece.stronghold.PortalRoom;
import kaptainwutax.featureutils.structure.generator.structure.StrongholdGenerator;
import kaptainwutax.mcutils.rand.ChunkRand;
import kaptainwutax.mcutils.util.math.Vec3i;
import kaptainwutax.mcutils.util.pos.BPos;
import kaptainwutax.mcutils.util.pos.CPos;
import kaptainwutax.mcutils.version.MCVersion;
import kaptainwutax.seedutils.rand.JRand;
import randomreverser.math.component.Vector;
import randomreverser.util.StringUtils;

import java.io.*;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        MCVersion version = MCVersion.v1_16_1;
        int minEyes = 0;
        int strongholds = 128;

        File filename = new File("src/main/java/pikachu/data/output.csv");
        File filename_extended = new File("src/main/java/pikachu/data/output_extended.csv");

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
            BufferedWriter extended = new BufferedWriter(new FileWriter(filename_extended));

            File seedFile = new File("./src/main/java/pikachu/data/seeds.csv");
            Scanner scanner = new Scanner(seedFile);

            List<Long> seeds = new ArrayList<>();

            while (scanner.hasNextLong()) {
                long seed = scanner.nextLong();
                seeds.add(seed);
            }

            scanner.close();

            for (Long seed : seeds) {
                System.out.printf("Locating strongholds for seed '%d'%n%n", seed);

                OverworldBiomeSource bs = new OverworldBiomeSource(version, seed);
                Stronghold stronghold = new Stronghold(version);

                try { extended.write("seed, starter_x, starter_z, eyes, portal_x, portal_y, portal_z\n"); }
                catch (IOException e) { e.printStackTrace(); }

                JRand jrand = new JRand(0L);

                CPos[] starts = stronghold.getStarts(bs, strongholds, jrand);
                for (CPos start : starts) {
                    StrongholdGenerator gen = new StrongholdGenerator(version);
                    int starterX = start.getX();
                    int starterY = start.getY();
                    int starterZ = start.getZ();

                    ChunkRand rand = new ChunkRand();
                    gen.populateStructure(seed, starterX, starterZ, rand);

                    gen.pieceList.forEach(piece -> {
                        if (piece instanceof PortalRoom) {
                            PortalRoom pr = (PortalRoom) piece;

//                            Vec3i portal_center = pr.
                            Vec3i portal_center = pr.getBoundingBox().getCenter();
                            int x = portal_center.getX();
                            int y = portal_center.getY();
                            int z = portal_center.getZ();

                            pr.process(jrand, new BPos(x, y, z));

                            int eyes = countTrue(pr.getEyes());

                            System.out.println("=== New Stronghold ===");

                            for (boolean eye : pr.getEyes()) {
                                System.out.println(eye);
                            }

                            //System.out.println(pr.getEyes().toString());
                            if (eyes >= minEyes) {
                                try {
                                    extended.write("Seed: %d, Starter X: %d, Starter Z: %d, Eyes: %d, X: %d, Y: %d, Z: %d\n".formatted(seed, (starterX * 16) + 4, (starterZ * 16) + 4, eyes, x, y, z));
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });

//                    if (gen.strongholdBox.maxY >= 63) {
//                        System.out.printf("!! Potential exposed stronghold starting at (%d, %d, %d)%n", (starterX * 16) + 4, gen.strongholdBox.maxY, (starterZ * 16) + 4);
//                    }
                }
            }
            writer.close();
            extended.close();

        } catch (IOException e) { e.printStackTrace(); }
    }

    public static int countTrue(boolean[] array) {
        int numTrue = 0;

        for (boolean item : array) {
            if (item) numTrue++;
        }

        return numTrue;
    }
}