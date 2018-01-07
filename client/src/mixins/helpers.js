export default {
  methods: {
    getCardUrl (card) {
      if (process.env.NODE_ENV === 'production') {
        return `/chicago/static/cards/${card.value}_${card.suit}.svg`
      }
      return `/static/cards/${card.value}_${card.suit}.svg`
    },
    getAvatarUrl (player) {
      return 'https://api.adorable.io/avatars/' + player.id
    }
  }
}
