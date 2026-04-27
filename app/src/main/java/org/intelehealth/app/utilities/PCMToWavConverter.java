package org.intelehealth.app.utilities;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class PCMToWavConverter {

    public static void pcmToWav(String pcmFilePath, String wavFilePath) throws IOException {

        int sampleRate = 4000;   // AyuSynk usually 4k or 8k
        int channels = 1;
        int bitDepth = 16;

        FileInputStream pcmInput = new FileInputStream(pcmFilePath);
        FileOutputStream wavOutput = new FileOutputStream(wavFilePath);

        long totalAudioLen = pcmInput.getChannel().size();
        long totalDataLen = totalAudioLen + 36;

        int byteRate = sampleRate * channels * bitDepth / 8;

        writeWavHeader(
                wavOutput,
                totalAudioLen,
                totalDataLen,
                sampleRate,
                channels,
                byteRate,
                bitDepth
        );

        byte[] buffer = new byte[1024];
        int bytesRead;

        while ((bytesRead = pcmInput.read(buffer)) != -1) {
            wavOutput.write(buffer, 0, bytesRead);
        }

        pcmInput.close();
        wavOutput.close();
    }

    private static void writeWavHeader(
            FileOutputStream out,
            long totalAudioLen,
            long totalDataLen,
            int sampleRate,
            int channels,
            int byteRate,
            int bitDepth
    ) throws IOException {

        byte[] header = new byte[44];

        // RIFF/WAVE header
        header[0] = 'R';
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';

        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);

        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';

        // fmt chunk
        header[12] = 'f';
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';

        header[16] = 16;
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;

        header[20] = 1;
        header[21] = 0;

        header[22] = (byte) channels;
        header[23] = 0;

        header[24] = (byte) (sampleRate & 0xff);
        header[25] = (byte) ((sampleRate >> 8) & 0xff);
        header[26] = (byte) ((sampleRate >> 16) & 0xff);
        header[27] = (byte) ((sampleRate >> 24) & 0xff);

        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);

        header[32] = (byte) (channels * bitDepth / 8);
        header[33] = 0;

        header[34] = (byte) bitDepth;
        header[35] = 0;

        // data chunk
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';

        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);

        out.write(header, 0, 44);
    }
}
