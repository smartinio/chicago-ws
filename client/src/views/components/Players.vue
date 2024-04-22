<template>
  <div class="is-flex is-flex-direction-column" style="width: 100%">
    <div v-for="(player, index) in players" style="padding-top: 15px">
      <Player
        variant="large"
        :isMe="false"
        :fallbackName="'Player ' + index + 1"
        :player="player"
        :currentPlayer="currentPlayer"
        :dealer="dealer"
        :chicagoTaker="chicagoTaker"
        :roundWinner="roundWinner"
      />
    </div>
  </div>
</template>
<script lang="ts">
import Player from '@/views/components/Player.vue'
export default {
  name: 'Players',
  props: ['players', 'currentPlayer', 'dealer', 'chicagoTaker', 'roundWinner'],
  components: {
    Player
  }
}
</script>
<style lang="scss" scoped>
</style>
