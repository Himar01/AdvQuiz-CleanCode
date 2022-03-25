package es.ulpgc.eite.da.quiz.question;

import android.util.Log;

import java.lang.ref.WeakReference;

import es.ulpgc.eite.da.quiz.app.AppMediator;
import es.ulpgc.eite.da.quiz.app.CheatToQuestionState;
import es.ulpgc.eite.da.quiz.app.QuestionToCheatState;

public class QuestionPresenter implements QuestionContract.Presenter {

  public static String TAG = QuestionPresenter.class.getSimpleName();

  private AppMediator mediator;
  private WeakReference<QuestionContract.View> view;
  private QuestionState state;
  private QuestionContract.Model model;

  public QuestionPresenter(AppMediator mediator) {
    this.mediator = mediator;
    state = mediator.getQuestionState();
  }

  @Override
  public void onStart() {
    Log.e(TAG, "onStart()");
    // call the model

    state.question = model.getQuestion();
    state.option1 = model.getOption1();
    state.option2 = model.getOption2();
    state.option3 = model.getOption3();

    // reset state to tests
    state.answerCheated=false;
    // update the view
    disableNextButton();
    view.get().resetReply();
  }


  @Override
  public void onRestart() {
    Log.e(TAG, "onRestart()");



  }


  @Override
  public void onResume() {
    Log.e(TAG, "onResume()");
    model.setQuizIndex(state.quizIndex);

    // use passed state if is necessary
    CheatToQuestionState savedState = getStateFromCheatScreen();

    if (savedState != null) {
      state.answerCheated = savedState.answerCheated;
    }

    if(state.optionClicked){
      onOptionButtonClicked(state.option);
    }else {
      view.get().resetReply();
    }
    if(state.answerCheated){
      if(!model.hasQuizFinished()){
        onNextButtonClicked();
      }else{
        state.optionEnabled=false;
      }

    }
    // update the view
    view.get().displayQuestion(state);
  }



  @Override
  public void onDestroy() {
    Log.e(TAG, "onDestroy()");
  }


@Override
public void onOptionButtonClicked(int option) {
  Log.e(TAG, "onOptionButtonClicked()");
  state.optionClicked=true;
  state.option=option;
  boolean isCorrect = model.isCorrectOption(option);
  if(isCorrect) {
    state.cheatEnabled=false;
  } else {
    state.cheatEnabled=true;
  }
  enableNextButton(isCorrect);
  view.get().updateReply(isCorrect);
}



  @Override
  public void onNextButtonClicked() {
    Log.e(TAG, "onNextButtonClicked()");
    disableNextButton();
    state.optionClicked=false;
    model.updateQuizIndex();
    state.quizIndex = model.getQuizIndex();
    state.question = model.getQuestion();
    state.option1 = model.getOption1();
    state.option2 = model.getOption2();
    state.option3 = model.getOption3();

    view.get().displayQuestion(state);
    view.get().resetReply();
  }

  @Override
  public void onCheatButtonClicked() {
    Log.e(TAG, "onCheatButtonClicked()");
        view.get().navigateToCheatScreen();
        QuestionToCheatState state = new QuestionToCheatState();
        passStateToCheatScreen(state);
  }

  private void passStateToCheatScreen(QuestionToCheatState state) {
    state.answer = model.getAnswer();
    mediator.setQuestionToCheatState(state);
  }

  private CheatToQuestionState getStateFromCheatScreen() {
    return mediator.getCheatToQuestionState();
  }

  private void disableNextButton() {
    state.optionEnabled=true;
    state.cheatEnabled= true;
    state.nextEnabled=false;
    //resets answerCheated
    state.answerCheated=false;

  }

  private void enableNextButton(boolean isCorrect) {
    boolean finished = model.hasQuizFinished();
    state.optionEnabled=false;
    state.cheatEnabled=!isCorrect;
    view.get().enableNextButton(isCorrect, finished);
    if(!finished) {
      state.nextEnabled=true;

    }
  }

  @Override
  public void injectView(WeakReference<QuestionContract.View> view) {
    this.view = view;
  }

  @Override
  public void injectModel(QuestionContract.Model model) {
    this.model = model;
  }

}
