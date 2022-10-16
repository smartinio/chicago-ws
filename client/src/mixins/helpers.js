export default {
  methods: {
    getCardUrl (card) {
      return `static/cards/${card.value}_${card.suit}.svg`
    },
    getAvatarUrl (player) {
      return `https://avatars.dicebear.com/api/adventurer-neutral/${player.id}.svg`
    }
  }
}
