import static java.lang.System.exit;

public class Pauvocoder {

    // Processing SEQUENCE size (100 msec with 44100Hz samplerate)
    final static int SEQUENCE = StdAudio.SAMPLE_RATE/10;

    // Overlapping size (20 msec)
    final static int OVERLAP = SEQUENCE/5 ;
    // Best OVERLAP offset seeking window (15 msec)
    final static int SEEK_WINDOW = 3*OVERLAP/4;

    public static void main(String[] args) {
        if (args.length < 2)
        {
            System.out.println("usage: pauvocoder <input.wav> <freqScale>\n");
            exit(1);
        }


        String wavInFile = args[0];
        double freqScale = Double.valueOf(args[1]);
        String outPutFile= wavInFile.split("\\.")[0] + "_" + freqScale +"_";

        // Open input .wev file
        double[] inputWav = StdAudio.read(wavInFile);

        // Resample test
        double[] newPitchWav = resample(inputWav, freqScale);
        StdAudio.save(outPutFile+"Resampled.wav", newPitchWav);

        /*// Simple dilatation
        double[] outputWav   = vocodeSimple(newPitchWav, 1.0/freqScale);
        StdAudio.save(outPutFile+"Simple.wav", outputWav);

        // Simple dilatation with overlaping
        outputWav = vocodeSimpleOver(newPitchWav, 1.0/freqScale);
        StdAudio.save(outPutFile+"SimpleOver.wav", outputWav);

        // Simple dilatation with overlaping and maximum cross correlation search
        outputWav = vocodeSimpleOverCross(newPitchWav, 1.0/freqScale);
        StdAudio.save(outPutFile+"SimpleOverCross.wav", outputWav);

        joue(outputWav);

        // Some echo above all
        outputWav = echo(outputWav, 100, 0.7);
        StdAudio.save(outPutFile+"SimpleOverCrossEcho.wav", outputWav);

        // Display waveform
        displayWaveform(outputWav);*/

    }

    /**
     * Resample inputWav with freqScale
     * @param inputWav
     * @param freqScale
     * @return resampled wav
     */
    public static double[] resample(double[] inputWav, double freqScale) {
        if (freqScale > 1) {
            double newSampleLeft = 1 - ((freqScale - 1) / freqScale); // 1.5freq => 0.66666666 left
            double[] resampledWav = new double[(int)(inputWav.length * newSampleLeft)];

            int forLimit = (int)(newSampleLeft * 100);
            int forStep = 0;
            int currentCounter = 0;

            // Compteur d'un saut de séquence pour laisser passer les valeurs au dessus, puis reboucler le compteur dans le for
            for (int i = 0; i < inputWav.length - 1; i++){
                if (forStep <= forLimit){
                    resampledWav[currentCounter] = inputWav[i];
                    currentCounter++;
                }

                if (forStep == 100){
                    forStep = 0;
                }
                forStep++;
            }
            return resampledWav;
        } else if (freqScale < 1) {
            double newSampleAdded = 1 + ((1 - freqScale) / freqScale); // 0.5freq => 2 added
            double[] resampledWav = new double[(int)(inputWav.length * newSampleAdded)];

            int forLimit = (int)(newSampleAdded * 100);
            int forStep = 0;
            int currentCounter = 0;

            // Compteur d'un saut de séquence pour laisser passer les valeurs au dessus, puis reboucler le compteur dans le for
            for (int i = 0; i < inputWav.length - 1; i++){
                if (forStep <= forLimit){
                    resampledWav[currentCounter] = inputWav[i];
                    currentCounter++;
                }

                if (forStep == 100){
                    forStep = 0;
                }
                forStep++;
            }
            return resampledWav;
        } else {
            return inputWav;
        }



        /*throw new UnsupportedOperationException("Not implemented yet");*/
    }

    /**
     * Simple dilatation, without any overlapping
     * @param inputWav
     * @param dilatation factor
     * @return dilated wav
     */
    public static double[] vocodeSimple(double[] inputWav, double dilatation) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * Simple dilatation, with overlapping
     * @param inputWav
     * @param dilatation factor
     * @return dilated wav
     */
    public static double[] vocodeSimpleOver(double[] inputWav, double dilatation) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * Simple dilatation, with overlapping and maximum cross correlation search
     * @param inputWav
     * @param dilatation factor
     * @return dilated wav
     */
    public static double[] vocodeSimpleOverCross(double[] inputWav, double dilatation) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * Play the wav
     * @param wav
     */
    public static void joue(double[] wav) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * Add an echo to the wav
     * @param wav
     * @param delay in msec
     * @param gain
     * @return wav with echo
     */
    public static double[] echo(double[] wav, double delay, double gain) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * Display the waveform
     * @param wav
     */
    public static void displayWaveform(double[] wav) {
        throw new UnsupportedOperationException("Not implemented yet");
    }


}
