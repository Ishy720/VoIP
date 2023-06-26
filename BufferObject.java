import java.nio.ByteBuffer;

public class BufferObject implements Comparable<BufferObject>
{
    public byte[] buffer;
    private int key = 25512361;
    public ByteBuffer wrapped;
    public int authCodeReceived;
    public int packetNumberReceived;

    public BufferObject(byte[] buffer) {
        this.buffer = buffer;
        this.wrapped = ByteBuffer.wrap(buffer);
        this.authCodeReceived = wrapped.getInt();
        this.packetNumberReceived = wrapped.getInt();
    }

    public byte[] getBuffer() {
        return buffer;
    }

    public int getAuthCode() {
        return this.authCodeReceived;
    }

    public int getPacketNumber() {
        return this.packetNumberReceived;
    }


    public byte[] retrievePayload() {
        //declare offset variable: audio packet is held 8 bytes into the array so this will ignore the headers
        int offset = 8;

        //declare byte array to hold the payload
        byte[] payload = new byte[512];

        //copy the payload from the packet to our payload array using arraycopy and offset
        System.arraycopy(wrapped.array(), offset, payload, 0, wrapped.array().length - offset);

        //wrap the payload in a bytebuffer so we can iterate through the integers and apply decryption
        ByteBuffer wrappedPayload = ByteBuffer.wrap(payload);

        //instantiate a new bytebuffer to store the decrypted payload
        ByteBuffer unwrappedPayload = ByteBuffer.allocate(payload.length);

        for(int i = 0; i < wrappedPayload.array().length / 4; i++) {
            int fourByte = wrappedPayload.getInt();
            fourByte = fourByte ^ key;
            unwrappedPayload.putInt(fourByte);
        }

        byte[] decryptedAudioBlock = unwrappedPayload.array();

        return decryptedAudioBlock;
    }


    @Override
    public int compareTo(BufferObject o)
    {
        if (packetNumberReceived == o.getPacketNumber())
            return 0;

        if (packetNumberReceived > o.getPacketNumber())
            return 1;

        return -1;
    }
}
