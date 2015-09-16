package org.lenskit.eval.traintest.metrics;

import com.google.common.base.Preconditions;

/**
 * Exponential (half-life) discounting.
 */
public class ExponentialDiscount implements Discount {
    private final double alpha;

    /**
     * Construct an exponential discount.
     * @param hl The half-life.
     */
    public ExponentialDiscount(double hl) {
        Preconditions.checkArgument(hl > 1, "half-life must be greater than 1");
        alpha = hl;
    }

    /**
     * Get the half-life of the discount function.
     * @return The half-life of the discount function.
     */
    public double getHalfLife() {
        return alpha;
    }

    @Override
    public double discount(int rank) {
        return 1 / Math.pow(2, (rank - 1) / (alpha - 1));
    }

    @Override
    public String toString() {
        return "ExponentialDiscount(" + alpha + ")";
    }
}
