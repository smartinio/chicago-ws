<script setup lang="ts">
import { useStore } from 'vuex'

const store = useStore()
</script>

<template>
  <section class="section">
    <div class="container">
      <ConnectionStatus :connected="connected && !checkingGame" />
      <section v-if="isAlreadyInAGame" class="hero is-warning" style="margin-top: 30px">
        <div class="hero-body">
          <p class="title">
            You're currently in a game
          </p>
          <p class="subtitle">
            Do you want to rejoin?
          </p>
          <p>
            <button class="button is-black" @click="rejoin">Yes, rejoin</button>
            <button class="button is-warning is-light" @click="leave">No, leave game</button>
          </p>
        </div>
      </section>

      <div class="tabs is-boxed is-large is-centered is-fullwidth" style="margin-top: 30px">
        <ul>
          <li :class="isTab('create') ? 'is-active' : ''">
            <a @click="tab = 'create'" class="rounded-tab">
              <span class="icon is-small"><i class="fas fa-plus"></i></span>
              <span>Create new game</span>
            </a>
          </li>
          <li :class="isTab('join') ? 'is-active' : ''">
            <a @click="tab = 'join'" class="rounded-tab">
              <span class="icon is-small"><i class="fas fa-people-group"></i></span>
              <span>Join existing game</span>
            </a>
          </li>
        </ul>
      </div>

      <div v-if="isTab('create')">
        <div class="field">
          <label class="label is-large">Nickname</label>
          <div class="control">
            <input :disabled="isAlreadyInAGame || checkingGame" @keyup.enter="handleKeyupEnter"
              :class="dangerIfExists(errors.nickname)" class="input is-large is-rounded" v-model="nickname" ref="nickname"
              placeholder="Choose a nickname..." maxlength="15">
            <p class="help is-danger" v-if="errors.nickname">
              {{ errors.nickname }}
            </p>
          </div>
        </div>

        <div id="wtf" class="is-flex is-justify-content-space-between">
          <div>
            <label class="label is-large">Rules</label>
            <div class="field is-grouped is-grouped-multiline">
              <div class="control block">
                <div class="select is-rounded">
                  <select v-model="rules.numTrades" :disabled="isAlreadyInAGame || checkingGame">
                    <option :value="1">1 trade</option>
                    <option :value="2">2 trades</option>
                    <option :value="3">3 trades</option>
                  </select>
                </div>
              </div>

              <div class="control block">
                <div class="select is-rounded">
                  <select v-model="rules.chicagoBefore15" :disabled="isAlreadyInAGame || checkingGame">
                    <option :value="false">Chicago: Needs 15p</option>
                    <option :value="true">Chicago: Anytime</option>
                  </select>
                </div>
              </div>

              <div class="control block">
                <div class="select is-rounded">
                  <select v-model="rules.roundWinScore" :disabled="isAlreadyInAGame || checkingGame">
                    <option :value="2">Round win: 2p</option>
                    <option :value="5">Round win: 5p</option>
                  </select>
                </div>
              </div>

              <div class="control block">
                <div class="select is-rounded">
                  <select v-model="rules.winWithTwoScore" :disabled="isAlreadyInAGame || checkingGame">
                    <option :value="5">Closing with Two: 5p</option>
                    <option :value="10">Closing with Two: 10p</option>
                  </select>
                </div>
              </div>

              <div class="control block">
                <div class="select is-rounded">
                  <select v-model="rules.tradeBanScore" :disabled="isAlreadyInAGame || checkingGame">
                    <option :value="41">Trade ban: 41p</option>
                    <option :value="42">Trade ban: 42p</option>
                    <option :value="45">Trade ban: 45p</option>
                    <option :value="46">Trade ban: 46p</option>
                  </select>
                </div>
              </div>

              <div class="control block">
                <div class="select is-rounded">
                  <select v-model="rules.chicagoBestHand" :disabled="isAlreadyInAGame || checkingGame">
                    <option :value="false">Chicago: All tricks</option>
                    <option :value="true">Chicago: + Best hand</option>
                  </select>
                </div>
              </div>

              <div class="control">
                <div class="select is-rounded">
                  <select v-model="rules.oneOpen" :disabled="isAlreadyInAGame || checkingGame">
                    <option value="ALL">1 open: All trades</option>
                    <option value="FINAL">1 open: Final trade</option>
                  </select>
                </div>
              </div>
            </div>
          </div>

          <div>
            <div class="field" style="margin-top: 18px">
              <div class="control">
                <button @click="createGame" :disabled="!hasNicknameSet || isAlreadyInAGame || checkingGame"
                  class="button is-success is-rounded is-large">
                  Create new game
                </button>
              </div>
            </div>
          </div>
        </div>

      </div>

      <div v-if="isTab('join')">
        <div class="field">
          <label class="label is-large">Nickname</label>
          <div class="control">
            <input :disabled="isAlreadyInAGame || checkingGame" @keyup.enter="handleKeyupEnter"
              :class="dangerIfExists(errors.nickname)" class="input is-large is-rounded" v-model="nickname" ref="nickname"
              placeholder="Choose a nickname..." maxlength="15">
            <p class="help is-danger" v-if="errors.nickname">
              {{ errors.nickname }}
            </p>
          </div>
        </div>

        <div class="field">
          <label class="label is-large">Invitation key</label>
          <div class="control">
            <input :disabled="isAlreadyInAGame || checkingGame" :class="dangerIfExists(errors.invKey)"
              class="input is-large is-rounded" type="text" placeholder="Invitation key" v-model="invKey">
            <p class="help is-danger" v-if="errors.invKey">
              {{ errors.invKey }}
            </p>
          </div>
        </div>

        <div class="field" style="margin-top: 30px">
          <div class="control">
            <button :disabled="!fieldsAreValid || isAlreadyInAGame || checkingGame"
              class="button is-info is-large is-rounded" @click="joinGame">
              Join game
            </button>
          </div>
        </div>
      </div>

    </div>
  </section>
</template>
<script lang="ts">
import { defineComponent } from 'vue'
import { SEND_ACTION } from '@/store/modules/socket/action_types'
import Action from '@/dto/action/Action'
import ConnectionStatus from '@/views/components/ConnectionStatus.vue'
import { OneOpenMode } from '@/server-types'

export default defineComponent({
  name: 'Start',
  components: {
    ConnectionStatus,
  },
  props: {
    currentlyInGame: {
      type: Boolean,
      default: undefined,
    },
    connected: {
      type: Boolean,
      required: true,
    },
    urlKey: {
      type: String,
      default: ''
    },
    errors: {
      type: Object,
      required: true
    }
  },
  data: () => {
    return {
      tab: 'create',
      invKey: '',
      nickname: '',
      leftGame: false,
      rules: {
        chicagoBefore15: false,
        chicagoBestHand: false,
        roundWinScore: 5,
        tradeBanScore: 45,
        winWithTwoScore: 10,
        numTrades: 2,
        oneOpen: 'ALL' as OneOpenMode,
      }
    }
  },
  methods: {
    isTab(tab: string) {
      return this.tab === tab
    },
    rejoin() {
      this.$emit('rejoin')
    },
    leave() {
      this.$emit('leave')
      this.leftGame = true
    },
    rememberNickname() {
      if (this.nickname) {
        localStorage.setItem('nickname', this.nickname)
      }
    },
    rememberRules() {
      localStorage.setItem('rules', JSON.stringify(this.rules))
    },
    createGame() {
      if (!this.hasNicknameSet) return
      const gameCreation = { nickname: this.nickname, rules: this.rules }
      const actionDTO = new Action('NEW_GAME', gameCreation)
      this.$emit('action', actionDTO)
      this.rememberNickname()
      this.rememberRules()
    },
    joinGame() {
      if (!this.fieldsAreValid) return
      const request =  { nickname: this.nickname, key: this.invKey }
      const actionDTO = new Action('JOIN_GAME', request)
      this.$emit('action', actionDTO)
      this.rememberNickname()
    },
    checkGame() {
      const storedInvitationKey = localStorage.getItem('invitationKey')
      const storedPlayerId = localStorage.getItem('playerId')

      if (storedInvitationKey && storedPlayerId) {
        const details = { playerId: storedPlayerId, key: storedInvitationKey }
        const actionDTO = new Action('CHECK_GAME', details)
        // @ts-ignore
        this.$store.dispatch(SEND_ACTION, actionDTO)
      } else {
        localStorage.removeItem('invitationKey')
        localStorage.removeItem('playerId')
        // @ts-ignore
        this.$store.commit('SET_CURRENTLY_IN_GAME', false) // yolo
      }
    },
    dangerIfExists(value: string) {
      return this.errors[value] ? 'is-danger' : ''
    },
    handleKeyupEnter() {
      if (this.invKey) {
        this.joinGame()
      } else {
        this.createGame()
      }
    }
  },
  computed: {
    checkingGame() {
      return this.currentlyInGame === undefined
    },
    isAlreadyInAGame() {
      return !this.leftGame && this.currentlyInGame
    },
    hasNicknameSet() {
      return this.nickname.length > 0
    },
    hasKeySet() {
      return this.invKey.length >= 8
    },
    hasErrors() {
      return this.errors.invKey.length > 0 ||
        this.errors.nickname.length > 0
    },
    fieldsAreValid() {
      return !this.hasErrors && this.hasNicknameSet && this.hasKeySet
    }
  },
  created() {
    this.invKey = this.urlKey || ''

    const nickname = localStorage.getItem('nickname')
    const rulesJson = localStorage.getItem('rules')

    if (rulesJson) {
      this.rules = {
        ...this.rules,
        ...JSON.parse(rulesJson),
      }
    }

    if (nickname) {
      this.nickname = nickname
    }
  },
  mounted() {
    if (this.invKey) this.tab = 'join'
    // @ts-ignore
    this.$refs.nickname.focus()
  },
  watch: {
    connected(conn) {
      if (conn) {
        this.checkGame()
      }
    },
    urlKey(key) {
      if (key) {
        this.tab = 'join'
      }
      this.invKey = key || this.invKey || ''
    },
    invKey() {
      this.$emit('changeKey')
    },
    nickname() {
      this.$emit('changeNick')
    }
  }
})
</script>

<style lang="scss">
.rounded-tab {
  border-top-left-radius: 20px !important;
  border-top-right-radius: 20px !important;
}
</style>
