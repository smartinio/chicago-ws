<template>
  <div>
    <span class="tag is-light" v-if="player.hasTakenChicago" style="margin-left: 10px">
      🚀
    </span>
    <span class="tag is-info" v-if="!isMe && isCurrentPlayer(player)" style="margin-left: 10px">
      PLAYING
    </span>
    <span class="tag is-dark" v-if="isDealer(player)" style="margin-left: 10px">
      DEALER
    </span>
    <span class="tag is-success" v-if="player.winner" style="margin-left: 10px">
      WINNER
    </span>
  </div>
</template>

<script lang="ts">
export default {
  name: 'PlayerStatus',
  props: ['player', 'currentPlayer', 'dealer', 'isMe'],
  methods: {
    isCurrentPlayer(player) {
      return this.currentPlayer && (this.currentPlayer.id === player.id)
    },
    isDealer(player) {
      return this.dealer && (this.dealer.id === player.id)
    }
  }
}
</script>
