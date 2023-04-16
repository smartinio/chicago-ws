export default class GameCreation {
  constructor (public nickname: string, public rules: any) {
    this.nickname = nickname
    this.rules = {
      chicagoBestHand: rules.chicagoBestHand,
      numTrades: rules.numTrades,
      oneOpen: rules.oneOpen,
    }
  }
}
