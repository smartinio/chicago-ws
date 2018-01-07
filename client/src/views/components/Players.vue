<template>
  <section class="section">
    <div class="container">
      <div class="columns">
        <div class="column" v-for="(player, index) in players">
          <div class="card">
            <div class="card-content">
              <div class="media">
                <div class="media-left">
                  <figure class="image is-48x48">
                    <img
                      :src="getAvatarUrl(player)"
                      alt="Placeholder image"
                      class="avatar"
                    >
                  </figure>
                </div>
                <div class="media-content">
                  <div class="columns">
                    <div class="column is-narrow">
                      <p class="title is-4">
                        {{ player.name || 'Player ' + (index+1) }}
                      </p>
                      <p class="subtitle is-6">
                        {{ player.score }} points
                      </p>
                    </div>
                    <div class="column is-expanded">
                      <div v-if="player.hand">
                        <figure
                          v-for="card in player.hand.played"
                          class="image is-inline-flex"
                          style="width: 33px; height: 48px"
                        >
                          <img
                            :src="getCardUrl(card)"
                            :alt="card.type + '' + card.value"
                            :class="isBaseCard(card) ? 'baseCard' : ''"
                          >
                        </figure>
                      </div>
                    </div>
                    <div class="column is-narrow">
                      <span
                        class="tag is-light"
                        v-if="player.hasTakenChicago"
                        style="margin-left: 10px"
                      >
                        C
                      </span>
                      <span
                        class="tag is-info"
                        v-if="isCurrentPlayer(player)"
                        style="margin-left: 10px"
                      >
                        THEIR TURN
                      </span>
                      <span
                        class="tag is-success"
                        v-if="player.winner"
                        style="margin-left: 10px"
                      >
                        WINNER
                      </span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>
<script>
export default {
  name: 'Players',
  props: ['players', 'baseMove', 'currentPlayer'],
  methods: {
    isBaseCard (card) {
      if (!this.baseMove) return false
      return (card.suit === this.baseMove.card.suit && card.value === this.baseMove.card.value)
    },
    isCurrentPlayer (player) {
      return this.currentPlayer && (this.currentPlayer.id === player.id)
    }
  }
}
</script>
<style lang="scss" scoped>

</style>
