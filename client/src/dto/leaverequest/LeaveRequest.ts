export default class LeaveRequest {
  constructor (public playerId: string, public key: string) {
    this.playerId = playerId
    this.key = key
  }
}
