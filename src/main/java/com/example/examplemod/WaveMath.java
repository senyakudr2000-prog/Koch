package com.example.examplemod;

/** Высота волны в точке (x, z) в момент времени t (секунды). Сумма синусоидальных волн. */
public final class WaveMath {
    // {направление x, направление z, длина волны (блоки), амплитуда (блоки)}
    private static final double[][] WAVES = {
            {1.0, 0.0, 12.0, 0.18},
            {0.7, 0.7, 7.0, 0.10},
            {-0.3, 1.0, 4.0, 0.05}
    };
    private static final double G = 9.81;

    private WaveMath() {}

    public static double height(double x, double z, double timeSeconds) {
        double h = 0;
        for (double[] w : WAVES) {
            double len = Math.hypot(w[0], w[1]);
            double k = 2 * Math.PI / w[2];
            double omega = Math.sqrt(G * k);
            double phase = k * ((w[0] * x + w[1] * z) / len) - omega * timeSeconds;
            h += w[3] * Math.sin(phase);
        }
        return h;
    }
}
