package ua.edu.sumdu.in71;

import java.util.StringJoiner;

public class Node {
    private int investment;
    private double profit;
    private int money;
    private Node next;
    private double discretness;

    public Node() {
    }

    public Node(int money) {
        this.money = money;
    }

    public Node getNext() {
        return next;
    }

    public void setNext(Node next) {
        this.next = next;
    }

    public int getMoney() {
        return money;
    }

    public void setMoney(int money) {
        this.money = money;
    }

    public double getProfit() {
        return profit;
    }

    public void setProfit(double profit) {
        this.profit = profit;
    }

    public int getInvestment() {
        return investment;
    }

    public void setInvestment(int investment) {
        this.investment = investment;
    }

    public double getDiscretness() {
        return discretness;
    }

    public void setDiscretness(double discretness) {
        this.discretness = discretness;
    }

    public double getInvestmentXdiscrentess() {
        return discretness * investment;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Node.class.getSimpleName() + "[", "]")
                .add("investment=" + investment)
                .add("profit=" + profit)
                .add("money=" + money)
                .add("next=" + next)
                .toString();
    }
}
