export default class Action {
  constructor (type, value) {
    if (typeof value !== typeof '') {
      value = JSON.stringify(value)
    }
    this.type = type
    this.value = value
  }
}
