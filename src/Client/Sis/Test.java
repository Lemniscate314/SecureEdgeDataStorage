package Client.Sis;

import Client.EndUserSIS;

import java.math.BigInteger;

public class Test {

    public static void main(String[] args) {
        // Example setup
        int l = 3;
        int m = 4;
        int N = 5;
        BigInteger[][] A = generateSampleMatrix(l, m);
        BigInteger q = BigInteger.valueOf(17);  // Replace with your actual q value

        // Create instances of EndUserSIS and DataManipulation
        EndUserSIS endUserSIS = new EndUserSIS(N);
        DataManipulation dataManipulation = new DataManipulation(l, N, A, q, endUserSIS);

        // Test deleteBlock
        BigInteger[][] VDelete = generateSampleMatrix(l, N);  // Replace with actual dataManipulation
        dataManipulation.deleteBlock(2, VDelete);
        printMatrix("VDelete", VDelete);

        // Test updateBlock
        BigInteger[][] XUpdate = generateSampleMatrix(m, 1);  // Replace with actual dataManipulation
        BigInteger[][] VUpdate = generateSampleMatrix(l, N);  // Replace with actual dataManipulation
        dataManipulation.updateBlock(1, XUpdate, VUpdate, "newBlock");
        printMatrix("VUpdate", VUpdate);

        // Test insertBlock
        BigInteger[][] XInsert = generateSampleMatrix(m, 1);  // Replace with actual dataManipulation
        BigInteger[][] VInsert = generateSampleMatrix(l, N);  // Replace with actual dataManipulation
        dataManipulation.insertBlock(3, XInsert, VInsert, "newDataBlock");
        printMatrix("VInsert", VInsert);
    }

    // Helper method to generate a sample matrix for testing
    private static BigInteger[][] generateSampleMatrix(int rows, int cols) {
        BigInteger[][] matrix = new BigInteger[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = BigInteger.valueOf(i * 10 + j);  // Replace with actual Block
            }
        }
        return matrix;
    }

    // Helper method to print a matrix for testing
    private static void printMatrix(String name, BigInteger[][] matrix) {
        System.out.println(name + ":");
        for (BigInteger[] row : matrix) {
            for (BigInteger value : row) {
                System.out.print(value + "\t");
            }
            System.out.println();
        }
        System.out.println();
    }
}
