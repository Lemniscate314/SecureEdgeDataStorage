package test;

public class SISFullTest {
    // Assuming IdentificationServer and EndUserSIS classes are available
/**
    @Test
    public void testMatrixADimensions() {
        IdentificationServer ibs = new IdentificationServer();
        BigInteger[][] matrixA = ibs.generateMatrixA();
        Assertions.assertEquals(ibs.getL(), matrixA.length); // l rows
        Assertions.assertEquals(ibs.getM(), matrixA[0].length); // m columns
    }

    @Test
    public void testMatrixXDimensions() {
        int lambda = 256; // Example lambda value
        int N = 10;       // Example number of Blocks blocks
        EndUserSIS dmc = new EndUserSIS(lambda, N);
        String[] dataBlocks = new String[N];
        for (int i = 0; i < N; i++) {
            dataBlocks[i] = "DataManipulation block " + (i + 1);
        }
        BigInteger[][] matrixX = dmc.constructMatrixX(dataBlocks);
        Assertions.assertEquals(N, matrixX.length); // N rows
        Assertions.assertEquals(dmc.getM(), matrixX[0].length); // m columns
    }

    @Test
    public void testMatrixVChangeOnDataBlockChange() {
        int lambda = 256;
        int N = 5;
        EndUserSIS dmc = new EndUserSIS(lambda, N);

        // Generate matrix V for one set of Blocks blocks
        String[] dataBlocks1 = {"Block1", "Block2", "Block3", "Block4", "Block5"};
        BigInteger[][] matrixV1 = dmc.computeMatrixV(dmc.constructMatrixX(dataBlocks1));

        // Generate matrix V for another set of Blocks blocks with one block changed
        String[] dataBlocks2 = {"Block1", "Block2", "Block3", "Block4", "Block6"};
        BigInteger[][] matrixV2 = dmc.computeMatrixV(dmc.constructMatrixX(dataBlocks2));
        // Check if matrix V changes when one Blocks block is changed
        boolean isDifferent = false;
        for (int i = 0; i < matrixV1.length; i++) {
            for (int j = 0; j < matrixV1[0].length; j++) {
                if (!matrixV1[i][j].equals(matrixV2[i][j])) {
                    isDifferent = true;
                    break;
                }
            }
            if (isDifferent) break;
        }

        assertTrue(isDifferent, "Matrix V should change when a Blocks block is changed");
    }

    @Test
    public void testHashFunctionH3() {
        int lambda = 256;
        int N = 5;
        EndUserSIS dmc = new EndUserSIS(lambda, N);
        String dataBlock = "Test DataManipulation";

        byte[] hash1 = dmc.hashFunctionH3(dataBlock);
        byte[] hash2 = dmc.hashFunctionH3(dataBlock);

        assertArrayEquals(hash1, hash2, "Hash function H3 should produce consistent results for the same input");

        byte[] hash3 = dmc.hashFunctionH3(dataBlock + " "); // Slightly different input
        assertFalse(java.util.Arrays.equals(hash1, hash3), "Hash function H3 should produce different results for different inputs");
    }

    @Test
    public void testMatrixVDimensions() {
        IdentificationServer ibs = new IdentificationServer();
        int lambda = 256;
        int N = 10;
        EndUserSIS dmc = new EndUserSIS(lambda, N);
        String[] dataBlocks = new String[N];
        for (int i = 0; i < N; i++) {
            dataBlocks[i] = "DataManipulation block " + (i + 1);
        }
        BigInteger[][] matrixX = dmc.constructMatrixX(dataBlocks);
        BigInteger[][] matrixV = dmc.computeMatrixV(matrixX);

        Assertions.assertEquals(ibs.getL(), matrixV.length); // l rows
        Assertions.assertEquals(N, matrixV[0].length); // N columns
    }**/
}

