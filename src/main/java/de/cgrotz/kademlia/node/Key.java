package de.cgrotz.kademlia.node;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.BitSet;

/**
 * Created by Christoph on 21.09.2016.
 */
@Data
@EqualsAndHashCode(of = "key")
public class Key {
    public final static int ID_LENGTH = 160;

    private BigInteger key;

    public Key(byte[] result) {
        if(result.length > ID_LENGTH/8 ) {
            throw new RuntimeException("ID to long. Needs to be  "+ID_LENGTH+"bits long." );
        }
        this.key = new BigInteger(result);
    }

    public Key(BigInteger key) {
        if( key.toByteArray().length > ID_LENGTH/8 ) {
            throw new RuntimeException("ID to long. Needs to be  "+ID_LENGTH+"bits long." );
        }
        this.key = key;
    }

    public Key(int id) {
        this.key = BigInteger.valueOf(id);
    }

    public static Key random() {
        byte[] bytes = new byte[ID_LENGTH / 8];
        SecureRandom sr1 = new SecureRandom();
        sr1.nextBytes(bytes);
        return new Key(bytes);
    }

    public static Key build(String key) {
        return new Key(new BigInteger(key));
    }

    /**
     * Checks the distance between this and another Key
     *
     * @param nid
     *
     * @return The distance of this Key from the given Key
     */
    public Key xor(Key nid)
    {
        return new Key(nid.getKey().xor(this.key));
    }

    /**
     * Generates a Key that is some distance away from this Key
     *
     * @param distance in number of bits
     *
     * @return Key The newly generated Key
     */
    public Key generateNodeIdByDistance(int distance)
    {
        byte[] result = new byte[ID_LENGTH / 8];

        /* Since distance = ID_LENGTH - prefixLength, we need to fill that amount with 0's */
        int numByteZeroes = (ID_LENGTH - distance) / 8;
        int numBitZeroes = 8 - (distance % 8);

        /* Filling byte zeroes */
        for (int i = 0; i < numByteZeroes; i++)
        {
            result[i] = 0;
        }

        /* Filling bit zeroes */
        BitSet bits = new BitSet(8);
        bits.set(0, 8);

        for (int i = 0; i < numBitZeroes; i++)
        {
            /* Shift 1 zero into the start of the value */
            bits.clear(i);
        }
        bits.flip(0, 8);        // Flip the bits since they're in reverse order
        result[numByteZeroes] = (byte) bits.toByteArray()[0];

        /* Set the remaining bytes to Maximum value */
        for (int i = numByteZeroes + 1; i < result.length; i++)
        {
            result[i] = Byte.MAX_VALUE;
        }

        return this.xor(new Key(result));
    }

    /**
     * Counts the number of leading 0's in this Key
     *
     * @return Integer The number of leading 0's
     */
    public int getFirstSetBitIndex()
    {
        int prefixLength = 0;

        for (byte b : this.key.toByteArray())
        {
            if (b == 0)
            {
                prefixLength += 8;
            }
            else
            {
                /* If the byte is not 0, we need to count how many MSBs are 0 */
                int count = 0;
                for (int i = 7; i >= 0; i--)
                {
                    boolean a = (b & (1 << i)) == 0;
                    if (a)
                    {
                        count++;
                    }
                    else
                    {
                        break;   // Reset the count if we encounter a non-zero number
                    }
                }

                /* Add the count of MSB 0s to the prefix length */
                prefixLength += count;

                /* Break here since we've now covered the MSB 0s */
                break;
            }
        }
        return prefixLength;
    }

    @Override
    public String toString()
    {
        return this.key.toString();
    }


    /**
     * Gets the distance from this Key to another Key
     *
     * @param to
     *
     * @return Integer The distance
     */
    public int getDistance(Key to)
    {
        /**
         * Compute the xor of this and to
         * Get the index i of the first set bit of the xor returned Key
         * The distance between them is ID_LENGTH - i
         */
        return ID_LENGTH - this.xor(to).getFirstSetBitIndex();
    }
}
