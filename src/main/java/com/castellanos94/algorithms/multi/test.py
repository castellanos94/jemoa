def train_and_perform(index, policy, episodes, n_steps, market_variables, indicators, reward_function, normalization_function, tickers, training_start_date, training_end_date, trading_start_date, trading_end_date, trading_fee, file_, nn_arch='MLP', window_width=1): df = get_dataframe(tickers, training_start_date, trading_end_date) df = feature_engineering(df, indicators)
  if not (normalization_function is None): df = normalization_function(df, tickers, market_variables + indicators)

    train_df = data_split(df, training_start_date, training_end_date) print(train_df)
calcular vector varianzas de train_df en base a los precios de cierre

trading_df = data_split(df, trading_start_date, trading_end_date)

if reward_function == 'pv': env_kwargs = { 'initial_amount': 1, 'stock_dim': len(tickers), 'tech_indicator_list': indicators, 'market_variables': market_variables, 'reward_function': reward_function, 'trading_fee': trading_fee, 'nn_arch': nn_arch, 'window_width': window_width, 'day': window_width - 1 } elif reward_function == 'sr': yf.pdr_override()

df_adj_close = retrieve_data_from_yahoo(tickers,
                                        training_start_date,
                                        training_end_date,
                                        'Adj Close')
df_adj_close, tickers_adj_close = data_cleaning(df_adj_close)

data_stddev, data_corr, expected_values = data_preparation(df_adj_close, tickers_adj_close)

free_risk_return = 0.015

env_kwargs = {
  'initial_amount': 1, 
  'stock_dim': len(tickers), 
  'tech_indicator_list': indicators, 
  'market_variables': market_variables,
  'expected_values': expected_values,
  'reward_function': reward_function,
  'free_risk_return': free_risk_return,
  'data_stddev': data_stddev,
  'data_corr': data_corr,
  'trading_fee': trading_fee,
  'nn_arch': nn_arch,
  'window_width': window_width,
  'day': window_width - 1
}

training_env_gym = PortfolioAllocation(df=train_df, **env_kwargs) training_env, _ = training_env_gym.get_sb_env()

trading_env_gym = PortfolioAllocation(df=trading_df, **env_kwargs) trading_env, _ = trading_env_gym.get_sb_env()

####### initialize environment hyperparameters ######

has_continuous_action_space = True max_ep_len = len(train_df.date.unique()) max_ep_trading_len = len(trading_df.date.unique())
max_training_timesteps = len(train_df.date.unique()) * episodes

print_freq = max_ep_len * 10 # print avg reward in the interval (in num timesteps) log_freq = max_ep_len * 2 # log avg reward in the interval (in num timesteps) save_model_freq = int(1e5) # save model frequency (in num timesteps)

action_std = 0.6 # starting std for action distribution (Multivariate Normal) action_std_decay_rate = 0.01 # linearly decay action_std (action_std = action_std - action_std_decay_rate) min_action_std = 0.1 # minimum action_std (stop decay after action_std <= min_action_std) action_std_decay_freq = int(5.5e3) # action_std decay frequency (in num timesteps)

#####################################################

################ PPO hyperparameters ################

update_timestep = max_ep_len * 4 # update policy every n timesteps

#update_timestep = int(max_ep_len / 4) K_epochs = 80 # update policy for K epochs in one PPO update

eps_clip = 0.2 # clip parameter for PPO gamma = 0.99 # discount factor

lr_actor = 0.0003 # learning rate for actor network lr_critic = 0.001 # learning rate for critic network

random_seed = 0 # set random seed if required (0 = no random seed)

#####################################################
state space dimension

state_dim = training_env.observation_space.shape[0]
action space dimension

action_dim = training_env.action_space.shape[0] action_var_hist = df.var() print('action_var_hist',action_var_hist) ppo_agent = PPO(state_dim, action_dim, #[0.2, 0.3, 0.5] aunque solamente tengas 2 activos lr_actor, lr_critic, gamma, K_epochs, eps_clip, has_continuous_action_space, action_var_hist, action_std,

              nn_arch=nn_arch,
              kernel_size=(len(market_variables) + len(indicators)) * window_width)

printing and logging variables

print_running_reward = 0 print_running_episodes = 0

time_step = 0 i_episode = 0

################ Training ################ training_time = time.time() while time_step <= max_training_timesteps: state = training_env.reset() current_ep_reward = 0

for t in range(1, max_ep_len+1):
  # select action with policy
  action = ppo_agent.select_action(state)
  state, reward, done, _ = training_env.step([action])

  reward = reward[0]

  # saving reward and is_terminals
  ppo_agent.buffer.rewards.append(reward)
  ppo_agent.buffer.is_terminals.append(done)

  time_step +=1
  current_ep_reward += reward

  # update PPO agent
  if time_step % update_timestep == 0:
    ppo_agent.update()

  # if continuous action space; then decay action std of ouput action distribution
  if has_continuous_action_space and time_step % action_std_decay_freq == 0:
    ppo_agent.decay_action_std(action_std_decay_rate, min_action_std)

  # break; if the episode is over
  if done:
    break

print_running_reward += current_ep_reward
print_running_episodes += 1

i_episode += 1

training_time = time.time() - training_time print('Training')

#####################################################

################ Testing ################

testing_time = 0 df_daily_return = None total_test_episodes = 15 for ep in range(total_test_episodes): testing_time_ = time.time() ep_reward = 0 state = trading_env.reset()

for t in range(max_ep_trading_len):
  action = ppo_agent.select_action(state)
  state, reward, done, _ = trading_env.step([action])

  reward = reward[0]
  ep_reward += reward

  if t == max_ep_trading_len - (window_width + 2):
    tmp_dr = trading_env.env_method(method_name="save_asset_memory")[0]
    actions_memory = trading_env.env_method(method_name="save_action_memory")[0]
    if df_daily_return is None:
      df_daily_return = tmp_dr
    else:
      df_daily_return['daily_return'] = df_daily_return['daily_return'] + tmp_dr['daily_return']
  if done:
    break

# clear buffer
ppo_agent.buffer.clear()
testing_time += (time.time() - testing_time_)

print('Testing') print(actions_memory)

#####################################################

df_daily_return.loc[:,'daily_return'] /= total_test_episodes df_plot = deepcopy(df_daily_return) df_plot['date'] = pd.to_datetime(df_plot['date']) df_plot.set_index("date", inplace=True, drop=True) df_plot.index = df_plot.index.tz_localize("UTC")

serie = pd.Series(df_plot["daily_return"], index=df_plot.index) print(serie) file_.write('training_time {}\n'.format(training_time)) file_.write('testing_time {}\n'.format(testing_time)) file_.write(str(pyfolio.timeseries.perf_stats(returns=serie))) serie.to_csv('2019-drl{}-{}-{}-{}.csv'.format(window_width, index, reward_function, normalization_function))

#print(pyfolio.timeseries.perf_stats(returns=serie)) pyfolio.create_full_tear_sheet(returns=serie)