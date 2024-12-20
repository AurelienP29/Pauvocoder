import static java.lang.System.exit;
import static java.lang.System.in;

public class Pauvocoder {

    // Processing SEQUENCE size (100 msec with 44100Hz samplerate)
    final static int SEQUENCE = StdAudio.SAMPLE_RATE / 10;

    // Overlapping size (20 msec)
    final static int OVERLAP = SEQUENCE / 5;
    // Best OVERLAP offset seeking window (15 msec)
    final static int SEEK_WINDOW = 3 * OVERLAP / 4;

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("usage: pauvocoder <input.wav> <freqScale>\n");
            exit(1);
        }


        String wavInFile = args[0];
        double freqScale = Double.valueOf(args[1]);
        String outPutFile = wavInFile.split("\\.")[0] + "_" + freqScale + "_";

        // Open input .wev file
        double[] inputWav = StdAudio.read(wavInFile);

        // Resample test
        double[] newPitchWav = resample(inputWav, freqScale);
        StdAudio.save(outPutFile + "Resampled.wav", newPitchWav);

        // Simple dilatation
        double[] outputWav = vocodeSimple(newPitchWav, 1.0 / freqScale);
        StdAudio.save(outPutFile + "Simple.wav", outputWav);

        // Simple dilatation with overlaping
        outputWav = vocodeSimpleOver(newPitchWav, 1.0/freqScale);
        StdAudio.save(outPutFile+"SimpleOver.wav", outputWav);

        // Simple dilatation with overlaping and maximum cross correlation search
        /*outputWav = vocodeSimpleOverCross(newPitchWav, 1.0/freqScale);
        StdAudio.save(outPutFile+"SimpleOverCross.wav", outputWav);*/

        /*joue(outputWav);*/

        // Some echo above all
        /*outputWav = echo(outputWav, 100, 0.7);
        StdAudio.save(outPutFile + "SimpleOverCrossEcho.wav", outputWav);*/

        // Display waveform
        /*displayWaveform(outputWav);*/

    }

    /**
     * Resample inputWav with freqScale
     *
     * @param inputWav
     * @param freqScale
     * @return resampled wav
     */
    public static double[] resample(double[] inputWav, double freqScale) {
        double[] newWav;

        if (freqScale > 1) {
            double substractSample = (freqScale - 1) / freqScale;
            newWav = new double[(int) (inputWav.length - (inputWav.length * substractSample))]; // Nouveau tableau, de la taille de toutes les sequences sans les sauté

        } else if (freqScale < 1) {
            double addedSample = (1 - freqScale) / freqScale;
            newWav = new double[(int) (inputWav.length + (inputWav.length * addedSample))]; // Nouveau tableau, de la taille de toutes les sequences sans les sauté

        } else {
            newWav = new double[inputWav.length];
        }

        double inputCounter = 0;
        int newCounter = 0;

        while (inputCounter < inputWav.length && newCounter < newWav.length) {
            newWav[newCounter] = inputWav[(int) (inputCounter)];

            newCounter++;
            inputCounter += freqScale;
        }

        return newWav;

    }

    /**
     * Simple dilatation, without any overlapping
     *
     * @param inputWav
     * @param dilatation factor
     * @return dilated wav
     */
    public static double[] vocodeSimple(double[] inputWav, double dilatation) {
        int seqJump = (int) (dilatation * SEQUENCE); // Taille du saut (illustration représentative)
        double[] newWav = new double[(int) (inputWav.length / dilatation)]; // Nouveau tableau de la bonne taille
        int inputCounter = 0, newCounter = 0;

        if (dilatation < 1) {
            int seqAdded = SEQUENCE - seqJump; // Taille de sequence ajouté

            while (inputCounter < inputWav.length && newCounter < newWav.length) {
                // Copie la sequence actuelle
                for (int i = 0; i < SEQUENCE && inputCounter < inputWav.length && newCounter < newWav.length; i++) {
                    newWav[newCounter] = inputWav[inputCounter];
                    inputCounter++;
                    newCounter++;
                }
                // Reviens en arrière
                inputCounter -= seqAdded;
            }
            return newWav;

        } else if (dilatation > 1) {
            int seqRemoved = (seqJump - SEQUENCE); // Taille de sequence supprimé

            while (inputCounter < inputWav.length && newCounter < newWav.length) {
                // Copie la sequence actuelle
                for (int i = 0; i < SEQUENCE && inputCounter < inputWav.length && newCounter < newWav.length; i++) {
                    newWav[newCounter] = inputWav[inputCounter];
                    inputCounter++;
                    newCounter++;
                }
                // Saute l'interval
                inputCounter += seqRemoved;
            }
            return newWav;
        } else {
            return inputWav;
        }
    }

    /**
     * Simple dilatation, with overlapping
     *
     * @param inputWav
     * @param dilatation factor
     * @return dilated wav
     */
    public static double[] vocodeSimpleOver(double[] inputWav, double dilatation) {
        int seqJump = (int) (dilatation * SEQUENCE); // Taille du saut (illustration représentative)
        int subSeq = SEQUENCE - OVERLAP;
        double attenuation = 1.0 / OVERLAP;
        double[] newWav = new double[(int) (inputWav.length / dilatation)]; // Nouveau tableau de la bonne taille

        int inputCounter = 0, newCounter = 0;
        int currentSeq = 0;


        if (dilatation < 1) {
            int seqAdded = subSeq - seqJump; // Taille de sequence ajouté

            while (inputCounter < inputWav.length && newCounter < newWav.length) {
                while (currentSeq < subSeq && inputCounter < inputWav.length && newCounter < newWav.length) {

                    if (currentSeq < OVERLAP) {
                        if (newWav[newCounter] == 0) {
                            newWav[newCounter] = inputWav[inputCounter] * (attenuation * currentSeq);
                        } else {
                            double addition = (newWav[newCounter] + (inputWav[inputCounter] * (attenuation * currentSeq)));
                            newWav[newCounter] = addition;
                        }

                    } else {
                        newWav[newCounter] = inputWav[inputCounter];
                    }

                    currentSeq++;
                    newCounter++;
                    inputCounter++;
                }

                for (int i = OVERLAP; i > 0 && inputCounter < inputWav.length && newCounter < newWav.length; i--) {
                    newWav[newCounter] = inputWav[inputCounter] * (attenuation * i);

                    newCounter++;
                    inputCounter++;
                }

                newCounter -= OVERLAP;

                inputCounter -= (seqAdded);
                currentSeq = 0;

                System.out.println("Input : " + inputCounter + ", New : " + newCounter);
            }
            return newWav;


        } else if (dilatation > 1) {
            int seqRemoved = seqJump - subSeq; // Taille de sequence supprimé

            while (inputCounter < inputWav.length && newCounter < newWav.length) {
                while (currentSeq < subSeq && inputCounter < inputWav.length && newCounter < newWav.length) {

                    if (currentSeq < OVERLAP) {
                        if (newWav[newCounter] == 0) {
                            newWav[newCounter] = inputWav[inputCounter] * (attenuation * currentSeq);
                        } else {
                            double addition = (newWav[newCounter] + (inputWav[inputCounter] * (attenuation * currentSeq)));
                            newWav[newCounter] = addition;
                        }

                    } else {
                        newWav[newCounter] = inputWav[inputCounter];
                    }

                    currentSeq++;
                    newCounter++;
                    inputCounter++;
                }

                for (int i = OVERLAP; i > 0 && inputCounter < inputWav.length && newCounter < newWav.length; i--) {
                    newWav[newCounter] = inputWav[inputCounter] * (attenuation * i);

                    newCounter++;
                    inputCounter++;
                }


                newCounter -= OVERLAP;


                inputCounter += (seqRemoved - (OVERLAP * 2));
                currentSeq = 0;

            }
            return newWav;

        } else {
            return inputWav;
        }
    }

    /**
     * Simple dilatation, with overlapping and maximum cross correlation search
     *
     * @param inputWav
     * @param dilatation factor
     * @return dilated wav
     */
    public static double[] vocodeSimpleOverCross(double[] inputWav, double dilatation) {
        int seqJump = (int) (dilatation * SEQUENCE); // Taille du saut (illustration représentative)
        int subSeq = SEQUENCE - OVERLAP;
        double attenuation = 1.0 / OVERLAP;
        double[] newWav = new double[(int) (inputWav.length / dilatation)]; // Nouveau tableau de la bonne taille

        int inputCounter = 0, newCounter = 0;
        int currentSeq = 0;
        boolean firstSeq = true;

        if (dilatation < 1) {
            int seqAdded = SEQUENCE - seqJump; // Taille de sequence ajouté

            while (inputCounter < inputWav.length && newCounter < newWav.length) {
                while (currentSeq < subSeq && inputCounter < inputWav.length && newCounter < newWav.length) {

                    if (currentSeq < OVERLAP) {
                        if (newWav[newCounter] == 0) {
                            newWav[newCounter] = inputWav[inputCounter] * (attenuation * currentSeq);
                        } else {
                            double addition = (newWav[newCounter] + (inputWav[inputCounter] * (attenuation * currentSeq)));
                            newWav[newCounter] = addition;
                        }

                    } else {
                        newWav[newCounter] = inputWav[inputCounter];
                    }

                    currentSeq++;
                    newCounter++;
                    inputCounter++;
                }

                for (int i = OVERLAP; i > 0 && inputCounter < inputWav.length && newCounter < newWav.length; i--) {
                    newWav[newCounter] = inputWav[inputCounter] * (attenuation * i);

                    newCounter++;
                    inputCounter++;
                }

                if (firstSeq) {
                    firstSeq = false;
                    newCounter -= OVERLAP;
                }

                inputCounter -= seqAdded;
                currentSeq = 0;

            }
            return newWav;


        } else if (dilatation > 1) {
            int seqRemoved = seqJump - SEQUENCE; // Taille de sequence supprimé

            while (inputCounter < inputWav.length && newCounter < newWav.length) {
                while (currentSeq < subSeq && inputCounter < inputWav.length && newCounter < newWav.length) {

                    if (currentSeq < OVERLAP) {
                        if (newWav[newCounter] == 0) {
                            newWav[newCounter] = inputWav[inputCounter] * (attenuation * currentSeq);
                        } else {
                            double addition = newWav[newCounter] + (inputWav[inputCounter] * (attenuation * currentSeq));
                            newWav[newCounter] = addition;
                        }

                    } else {
                        newWav[newCounter] = inputWav[inputCounter];
                    }

                    currentSeq++;
                    newCounter++;
                    inputCounter++;
                }

                for (int i = OVERLAP; i > 0 && inputCounter < inputWav.length && newCounter < newWav.length; i--) {
                    newWav[newCounter] = inputWav[inputCounter] * (attenuation * i);

                    newCounter++;
                    inputCounter++;
                }

                if (firstSeq) {
                    firstSeq = false;
                    newCounter -= OVERLAP;
                }

                if (inputCounter + seqRemoved + SEEK_WINDOW < inputWav.length) {
                    inputCounter = getBestCross(inputWav, inputCounter, inputCounter + seqRemoved);
                } else {
                    inputCounter += seqRemoved;
                }
                currentSeq = 0;

            }
            return newWav;

        } else {
            return inputWav;
        }
    }


    /**
     * Return the position where the next sequence should start, depending on the best correspondence
     *
     * @param wav
     * @param decStart
     * @param lookStart
     * @return
     */
    public static int getBestCross(double[] wav, int decStart, int lookStart) {
        int suivStartLow = lookStart;
        int suivStartHigh = lookStart;
        double neededMatch = wav[decStart];
        double currentMatchLow = -1;
        double currentMatchHigh = 1;

        for (int i = 0; i < SEEK_WINDOW; i++) {
            double currentValue = wav[lookStart + i];
            if (currentValue >= currentMatchLow && currentValue <= neededMatch) {
                currentMatchLow = currentValue;
                suivStartLow = lookStart + i;
            } else if (currentValue <= currentMatchHigh && currentValue >= neededMatch) {
                currentMatchHigh = currentValue;
                suivStartHigh = lookStart + i;
            }
        }

        double diffLow = neededMatch - currentMatchLow;
        double diffHigh = currentMatchHigh - neededMatch;

        if (diffLow < diffHigh) {
            return suivStartLow;
        } else {
            return suivStartHigh;
        }
    }


    /**
     * Play the wav
     *
     * @param wav
     */
    public static void joue(double[] wav) {
        initWindow(windowSize);

        int current = 0;
        while (current + windowSize < wav.length) {
            double[] wavTemp = new double[windowSize];
            for (int i = 0; i < windowSize; i++) {
                wavTemp[i] = wav[current + i];
                StdAudio.play(wav[current + i]);
            }
            displayWaveform(wavTemp);

            current += windowSize;
        }

        StdDraw.close();
        isWindowInit=false;
//        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * Add an echo to the wav
     *
     * @param wav
     * @param delay in msec
     * @param gain
     * @return wav with echo
     */
    public static double[] echo(double[] wav, double delay, double gain) {
        if (gain < -1 || gain > 1) {
            throw new IllegalArgumentException("Gain must be between -1 and 1");
        }

        int delaySamples = (int) Math.round(delay * 10);
        double[] echoedWav = new double[wav.length + delaySamples];

        for (int i = 0; i < wav.length; i++) {
            echoedWav[i] = wav[i];

            int echoIndex = i + delaySamples;
            if (echoIndex < echoedWav.length) {
                echoedWav[echoIndex] += wav[i] * gain;
            }
        }

        return echoedWav;
    }

    /**
     * Display the waveform
     *
     * @param wav
     */
    public static void displayWaveform(double[] wav) {
        // TODO : PB, tps de chargement d'un grand tableau wav trop grand !! => div en pls sous tab a afficher ? => condition d'activation de cette division ?
        initWindow(wav.length);
        StdDraw.clear();
        // Ligne de l'abscisse
        StdDraw.setPenColor(StdDraw.GRAY);
        StdDraw.line(0, 0, wav.length, 0);

        StdDraw.setPenColor(StdDraw.BLUE);
        for (int i = 1; i < wav.length; i+=20) {
            StdDraw.line(i, -wav[i], i, wav[i]);
        }
        StdDraw.show();
        // for (int i = 1, j = 1; j < wav.length; i++, j+=2) {
        //            StdDraw.line(i, -wav[j], i, wav[j]);
        //        }
    }

    // TODO : Fix possible pour le pb de création de window si appel displayWaveform individuel (sans joue)
    private static boolean isWindowInit = false;
    private static int windowSize = 4000;

    public static void initWindow(int windowSize) {
        if (!isWindowInit) {
            StdDraw.enableDoubleBuffering();
            StdDraw.setCanvasSize(1875, 750);
            StdDraw.setXscale(0, windowSize);
            StdDraw.setYscale(-1, 1);
            isWindowInit = true;
        }
    }


}
