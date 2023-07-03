import java.net.DatagramPacket;
import java.util.Vector;

public class Interleaver {

    final public int[] generatedArray;
    final int blockSize;
    final int delay;
    final int spread;

    // Construct Interleaver Block Size = (x * x)
    // Only works on block sizes of powers of 2, need to figure out how to do odd sizes etc
    // Matrix rotation will allow for all sizes to work (obviously not 1)
    public Interleaver(int x) throws Exception {

        if (x == 1) {
            throw new Exception("Cannot interleave a 1x1 matrix.");
        }

        blockSize = x * x;
        delay = blockSize - x;
        spread = x;
        generatedArray = new int[blockSize];
    }

    public int getSpread() {
        return this.spread;
    }

    public DatagramPacket[][] populateArray(Vector<DatagramPacket> specifiedArray) {
        DatagramPacket[][] constructedArray = new DatagramPacket[spread][spread];
        int arrayAccessCounter = 0;
        for (int i = 0; i < spread; i++) {
            for (int j = 0; j < spread; j++) {
                constructedArray[i][j] = specifiedArray.get(arrayAccessCounter);
                arrayAccessCounter += 1;
            }
        }

        return constructedArray;
    }
}
