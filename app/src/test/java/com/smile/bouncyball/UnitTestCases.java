package com.smile.bouncyball;

import android.graphics.Bitmap;

import com.smile.bouncyball.models.BouncyBall;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

public class UnitTestCases {

    int ballX;       //  coordinate (x-axis) of the ball
    int ballY;       //  coordinate (y-axis) of the ball
    int ballSize = 16;          // size of the ball
    int ballRadius = ballSize/2;
    int ballSpan = 8;           // speed of the ball
    int direction = 0;
    Bitmap bitmap = null;

    BouncyBall bBall = null;

    @BeforeClass
    public static void test_Setup() {
        System.out.println("Initializing before all test cases. One time running.");
    }

    @Before
    public void test_PreRun() {
        System.out.println("Setting up before each test case.");
        bBall = new BouncyBall(ballX, ballY, ballSize, ballSpan, bitmap);
    }

    @Test
    public void test_getBallRadius() {
        System.out.println("Testing the method getBallRadius() of BouncyBall class");
        int radius = bBall.getBallRadius();
        assertEquals(8, radius);
    }

    @After
    public void test_PostRun() {
        System.out.println("Cleaning up after each test case.");
    }

    @AfterClass
    public static void test_CleanUp() {
        System.out.println("Cleaning up after all test cases. One time running.");
    }
}
