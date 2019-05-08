package ua.edu.sumdu.in71;

public class DynamicProgrammingSolution {

    /**
     * @param           node the initial state summary
     * @param           whom the number of the unit (e.g. enterprise who we are going to distribute the money to.
     *                       usually it is equal to 0)
     * @param           discreteDistribution is a container for the discrete distributing functions of profit
     *                                       we get when invest in a unit
     * */
    public static void buildMostProfitDistributionPlan(Node node, int whom, DiscreteDistribution discreteDistribution) {
        if (whom == discreteDistribution.getAmOfUnits() - 1) {
            node.setNext(null);
            node.setInvestment(node.getMoney());
            node.setProfit(discreteDistribution.getProfit(whom, node.getMoney()));
            return;
        }
        Node[] childNodes = new Node[node.getMoney() + 1];
        for (int i = 0; i < childNodes.length; i++) {
            childNodes[i] = new Node();
            childNodes[i].setMoney(node.getMoney() - i);
            childNodes[i].setProfit(discreteDistribution.getProfit(whom + 1, i));
            childNodes[i].setInvestment(i);
            childNodes[i].setDiscretness(node.getDiscretness());
            buildMostProfitDistributionPlan(childNodes[i], whom + 1, discreteDistribution);
        }
        int mostProfitInvestment = indexOfTheMostProfitChild(childNodes, discreteDistribution, whom);
        node.setInvestment(mostProfitInvestment);
        node.setProfit(discreteDistribution.getProfit(whom, mostProfitInvestment));
        node.setNext(childNodes[mostProfitInvestment]);

    }

    private static int indexOfTheMostProfitChild(Node[] children, DiscreteDistribution discreteDistribution, int whom) {
        int mostProfitIndex = children.length - 1;
        double mostProfit = getTotalProfit(children[mostProfitIndex]) + discreteDistribution.getProfit(whom, mostProfitIndex);
        for (int i = 0; i < children.length - 1; i++) {
            if (getTotalProfit(children[i]) + discreteDistribution.getProfit(whom, i) >  mostProfit) {
                mostProfitIndex = i;
                mostProfit = getTotalProfit(children[i]) + discreteDistribution.getProfit(whom, i);
            }
        }
        return mostProfitIndex;
    }

    private static double getTotalProfit(Node node) {
        double totalProfit = 0;
        while (node != null) {
            totalProfit += node.getProfit();
            node = node.getNext();
        }
        return totalProfit;
    }
}
