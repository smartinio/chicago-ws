import { ActionType, GameCreation, JoinRequest, PlayingCard, RejoinRequest } from "@/server-types"

type ActionValue = JoinRequest | RejoinRequest | GameCreation | PlayingCard | PlayingCard[] | string | boolean

export default class Action {
  value: string

  constructor (public type: ActionType, value: ActionValue | null = null) {
    if (typeof value !== 'string') {
      value = JSON.stringify(value)
    }
    this.type = type
    this.value = value
  }
}
