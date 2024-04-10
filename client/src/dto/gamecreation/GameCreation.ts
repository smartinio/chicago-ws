export default class GameCreation {
  constructor (public nickname: string, public rules: any) {
    this.nickname = nickname
    this.rules = {
      chicagoBefore15: rules.chicagoBefore15,
      chicagoBestHand: rules.chicagoBestHand,
      numTrades: rules.numTrades,
      oneOpen: rules.oneOpen,
      roundWinScore: rules.roundWinScore,
      tradeBanScore: rules.tradeBanScore,
      winWithTwoScore: rules.winWithTwoScore,
    }
  }
}
