import GameCreation from "../gamecreation/GameCreation"
import JoinRequest from "../joinrequest/JoinRequest"
import RejoinRequest from "../rejoinrequest/RejoinRequest"

type Value = JoinRequest | RejoinRequest | GameCreation | string

export default class Action {
  value: string

  constructor (public type: string, value: Value) {
    if (typeof value !== 'string') {
      value = JSON.stringify(value)
    }
    this.type = type
    this.value = value
  }
}
