<template>
  <div class="is-flex is-flex-direction-column is-justify-content-space-between"
    style="background-color: #f5f5f5; border-radius: 20px; padding: 15px">
    <div class="is-flex is-flex-direction-column" style="flex: 1; overflow: hidden">
      <div class="is-flex is-justify-content-space-between">
        <ConnectionStatus :connected="connected" />
        <button class="button is-small is-rounded is-info is-light" @click="showRules = true" v-if="!showRules">
          <span class="icon is-small"><i class="fa fa-scale-balanced"></i></span>
          <span>Show rules</span>
        </button>
        <button class="button is-small is-rounded is-danger is-light" @click="$emit('leave')">
          <span class="icon is-small"><i class="fa fa-right-from-bracket"></i></span>
          <span>Leave game</span>
        </button>
        <button class="button is-small is-rounded is-gray is-light" @click="toggleHide">
          <span class="icon is-small"><i class="fa" :class="hidden ? 'fa-arrow-right' : 'fa-arrow-left'"></i></span>
          <span v-if="!hidden">Hide</span>
        </button>
      </div>

      <div class="is-flex" style="width: 400px" v-if="showRules">
        <div class="notification is-info" style="flex: 1">
          <button class="delete" aria-label="delete" @click="showRules = false"></button>
          <p><strong>{{ numTrades }}</strong> {{ numTrades == 1 ? 'trade' : 'trades' }}</p>
          <p>Chicago <strong>{{ chicagoBefore15 ? 'can be called' : 'cannot be called'}}</strong> before 15p</p>
          <p>Chicago <strong>{{ chicagoBestHand ? 'requires' : 'does not require' }}</strong> best hand</p>
          <p>1 open card available at <strong>{{ oneOpenFinal ? 'the final trade' : 'every trade' }}</strong></p>
          <p>Trading is banned at <strong>{{ tradeBanScore }}p</strong></p>
          <p>Winning a round gives <strong>{{ roundWinScore }}p</strong></p>
          <p>Closing with a Two gives <strong>{{ winWithTwoScore }}p</strong></p>
        </div>
      </div>

      <div style="height: 15px" />
      <EventLog :game="game" />
    </div>
    <div style="padding-top: 15px">
      <ChatSender />
    </div>
  </div>
</template>

<script lang="ts">
import ConnectionStatus from './ConnectionStatus.vue'
import ChatSender from './ChatSender.vue'
import EventLog from './EventLog.vue'

export default {
  name: 'Chat',
  props: ['game', 'connected'],
  data() {
    return {
      showRules: true,
      hidden: false,
    }
  },
  methods: {
    toggleHide() {
      this.$emit(this.hidden ? 'show' : 'hide')
      this.hidden = !this.hidden
    }
  },
  computed: {
    numTrades() {
      return this.game.rules.numTrades
    },
    chicagoBestHand() {
      return this.game.rules.chicagoBestHand
    },
    oneOpenFinal() {
      return this.game.rules.oneOpen === 'FINAL'
    },
    chicagoBefore15() {
      return this.game.rules.chicagoBefore15;
    },
    roundWinScore() {
      return this.game.rules.roundWinScore;
    },
    winWithTwoScore() {
      return this.game.rules.winWithTwoScore;
    },
    tradeBanScore() {
      return this.game.rules.tradeBanScore;
    },
  },
  components: {
    ChatSender,
    EventLog,
    ConnectionStatus,
  }
}
</script>
