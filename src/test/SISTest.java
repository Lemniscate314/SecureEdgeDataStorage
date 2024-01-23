package test;

public class SISTest {
    /**
    public static void main(String[] args) {
        // Test IdentificationServer
        IdentificationServer ibs = new IdentificationServer();

        // Test Static Variables
        System.out.println("Static variable 'a': " + IdentificationServer.a);
        System.out.println("Static variable 'l': " + IdentificationServer.l);
        System.out.println("Static variable 'q': " + IdentificationServer.q);
        System.out.println("Static variable 'm': " + IdentificationServer.m);

        // Test Matrix A Generation
        BigInteger[][] matrixA = ibs.generateMatrixA();
        System.out.println("Matrix A:");
        printMatrix(matrixA);

        // Initialize DataMatrixConstruction with lambda and N
        int lambda = 256; // Example lambda value
        int N = 3;       // Example number of data blocks
        DataMatrixConstruction dmc = new DataMatrixConstruction(lambda, N);

        // Prepare data blocks for testing
        String[] dataBlocks = new String[N];
        for (int i = 0; i < N; i++) {
            dataBlocks[i] = "Data block " + (i + 1);
        }
        System.out.println("\nData blocks:");
        System.out.println(Arrays.toString(dataBlocks));

        // Test Matrix X Construction
        BigInteger[][] matrixX = dmc.constructMatrixX(dataBlocks);
        System.out.println("\nMatrix X:");
        printMatrix(matrixX);

        // Test Matrix V Computation
        BigInteger[][] matrixV = dmc.computeMatrixV(matrixX);
        System.out.println("\nMatrix V:");
        printMatrix(matrixV);
    }

    // Utility method to print a matrix
    private static void printMatrix(BigInteger[][] matrix) {
        for (BigInteger[] row : matrix) {
            for (BigInteger val : row) {
                System.out.print(val + " ");
            }
            System.out.println();
        }
    }*/
}
