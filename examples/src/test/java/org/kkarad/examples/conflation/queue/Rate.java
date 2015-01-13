package org.kkarad.examples.conflation.queue;

class Rate {
    public String ccyPair;
    public double price;

    Rate(String ccyPair, double price) {
        this.ccyPair = ccyPair;
        this.price = price;
    }

    @Override
    public String toString() {
        return "Rate{" +
                "ccyPair='" + ccyPair + '\'' +
                ", price=" + price +
                '}';
    }
}
